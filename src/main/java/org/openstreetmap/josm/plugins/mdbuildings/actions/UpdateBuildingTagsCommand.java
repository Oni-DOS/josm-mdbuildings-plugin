package org.openstreetmap.josm.plugins.mdbuildings.actions;

import static org.openstreetmap.josm.plugins.mdbuildings.PreCheckUtils.isBuildingValueSimplification;
import static org.openstreetmap.josm.plugins.mdbuildings.TagConflictUtils.resolveTagConflictsDefault;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.mdbuildings.ImportStatus;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;


public class UpdateBuildingTagsCommand extends Command implements CommandResultBuilding, CommandWithErrorReason {

    static final String DESCRIPTION_TEXT = tr("Updated building tags");
    private final CommandResultBuilding resultSelectedBuilding;
    private final Way newBuilding;
    private boolean ignoreNoUpdate;
    private Way selectedBuilding;
    private SequenceCommand updateTagsCommand;

    private String executeErrorReason;
    private ImportStatus executeErrorStatus;

    public UpdateBuildingTagsCommand(DataSet dataSet, CommandResultBuilding resultSelectedBuilding, Way newBuilding) {
        super(dataSet);
        this.resultSelectedBuilding = resultSelectedBuilding;
        this.newBuilding = newBuilding;
        this.updateTagsCommand = null;
        this.ignoreNoUpdate = false;
    }

    public UpdateBuildingTagsCommand(
        DataSet dataSet, CommandResultBuilding resultSelectedBuilding, Way newBuilding, boolean ignoreNoUpdate
    ) {
        this(dataSet, resultSelectedBuilding, newBuilding);
        this.ignoreNoUpdate = ignoreNoUpdate;
    }

    @Override
    public void fillModifiedData(
        Collection<OsmPrimitive> modified,
        Collection<OsmPrimitive> deleted,
        Collection<OsmPrimitive> added
    ) {
        modified.add(selectedBuilding);
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        Collection<OsmPrimitive> primitives = new ArrayList<>();
        if (selectedBuilding != null) {
            primitives.add(selectedBuilding);  // Tags change
        }
        return primitives;
    }

    @Override
    public void undoCommand() {
        if (updateTagsCommand != null) {
            updateTagsCommand.undoCommand();
        }
    }

    @Override
    public String getDescriptionText() {
        return DESCRIPTION_TEXT;
    }

    private boolean shouldRemoveSourceTag(Way selBuilding) {
        if (!BuildingsSettings.AUTOREMOVE_UNWANTED_SOURCE.get()) {
            return false;
        }
        String source = selBuilding.get("source");
        if (source == null) {
            return false;
        }

        return BuildingsSettings.UNWANTED_SOURCE_VALUES.get()
            .stream()
            .map(String::toLowerCase)
            .anyMatch(val -> source.toLowerCase().contains(val));
    }

    private void handleUnwantedSourceTag(TagCollection tagsOfPrimitives) {
        tagsOfPrimitives.removeByKey("source");
        tagsOfPrimitives.add(new Tag("source", ""));
    }

    /**
     * Replace building with construction value for tag resolver to prevent conflicts and handle construction=* leftover
     */
    private void handleConstructionSubtag(TagCollection tagsOfPrimitives, Way selBuilding, Way nBuilding) {
        if (selBuilding.hasTag("building", "construction")
            && selBuilding.hasTag("construction")
            && !nBuilding.hasTag("building", "construction")) {

            // Remove from both, and re-add below if value doesn't simplify – it can be handled after this method
            tagsOfPrimitives.removeByKey("building");
            tagsOfPrimitives.removeByKey("building");

            if (isBuildingValueSimplification(selBuilding.get("construction"), nBuilding.get("building"))) {
                tagsOfPrimitives.add(new Tag("building", selBuilding.get("construction")));
            } else {
                tagsOfPrimitives.add(new Tag("building", selBuilding.get("construction")));
                tagsOfPrimitives.add(new Tag("building", nBuilding.get("building")));
            }

            tagsOfPrimitives.removeByKey("construction");
            tagsOfPrimitives.add(new Tag("construction", "")); // Remove completely construction tag
        }
    }

    /**
     * Handle source:geometry and source:building cleanup to ensure they are removed from MD-Buildings imports.
     */
    private void handleSourceTagsCleanup(TagCollection tagsOfPrimitives) {
        tagsOfPrimitives.removeByKey("source:geometry");
        tagsOfPrimitives.add(new Tag("source:geometry", "")); // Remove completely

        tagsOfPrimitives.removeByKey("source:building");
        tagsOfPrimitives.add(new Tag("source:building", "")); // Remove completely

        tagsOfPrimitives.removeByKey("layer_name");
        tagsOfPrimitives.add(new Tag("layer_name", ""));

        tagsOfPrimitives.removeByKey("medium");
        tagsOfPrimitives.add(new Tag("medium", ""));
    }

    @Override
    public boolean executeCommand() {
        if (this.updateTagsCommand == null) {
            this.selectedBuilding = resultSelectedBuilding.getResultBuilding();
            List<Command> commands;
            try {
                commands = new ArrayList<>(prepareUpdateTagsCommands(selectedBuilding, newBuilding));
            } catch (UserCancelException exception) {
                Logging.debug(
                    "No building tags (id: {0}) update, caused: Tag conflict dialog canceled by user",
                    selectedBuilding.getId()
                );
                executeErrorReason = tr("Tag conflict dialog canceled by user");
                executeErrorStatus = ImportStatus.CANCELED;
                return false;
            }

            // Ensure at least one command as result for UpdateBuildingsTagsCommand execute.
            // Edge-case for the FullImportStartegy: if geometry is updated and tags have no update
            // SequenceCommand doesn't allow to modify "sequenceComplete" which is needed to run undo() on manually
            // commited SequenceCommand, so put fake data change to execute SequenceCommand fully instead of manually.
            if (commands.isEmpty() && ignoreNoUpdate) {
                commands.add(new ChangePropertyCommand(newBuilding, "building",  newBuilding.get("building")));
            }

            if (commands.isEmpty()) {
                Logging.debug("No tags difference! Canceling!");
                executeErrorReason = tr("No tags to change");
                executeErrorStatus = ImportStatus.NO_UPDATE;
                return false;
            }
            this.updateTagsCommand = new SequenceCommand(DESCRIPTION_TEXT, commands);
        }
        this.updateTagsCommand.executeCommand();
        Logging.debug("Updated tags for the building: {0}", selectedBuilding);
        return true;
    }


    /**
     * Prepare update tags command using CombinePrimitiveResolverDialog before launching dialog.
     * It checks if any conflict can be skipped using resolveTagConflictsDefault from TagConflictsUtil
     *
     * @return list of commands as updating tags
     * @throws UserCancelException if user close the window or reject possible tags conflict
     */
    protected List<Command> prepareUpdateTagsCommands(
        Way selBuilding,
        Way nBuilding
    ) throws UserCancelException {
        Collection<OsmPrimitive> primitives = Arrays.asList(selectedBuilding, newBuilding);
        TagCollection tagsOfPrimitives = TagCollection.unionOfAllPrimitives(primitives);

        if (shouldRemoveSourceTag(selBuilding)) {
            handleUnwantedSourceTag(tagsOfPrimitives);
        }
        handleSourceTagsCleanup(tagsOfPrimitives);
        handleConstructionSubtag(tagsOfPrimitives, selBuilding, nBuilding);
        resolveTagConflictsDefault(tagsOfPrimitives, selBuilding, nBuilding);

        return CombinePrimitiveResolverDialog.launchIfNecessary(
            tagsOfPrimitives,
            primitives,
            Collections.singleton(selBuilding)
        );
    }

    @Override
    public Way getResultBuilding() {
        return this.selectedBuilding;
    }

    @Override
    public String getErrorReason() {
        return executeErrorReason;
    }

    @Override
    public ImportStatus getErrorStatus() {
        return executeErrorStatus;
    }
}
