package org.openstreetmap.josm.plugins.mdbuildings.actions;

import static org.openstreetmap.josm.plugins.mdbuildings.PostCheckUtils.findLifecyclePrefixBuildingTags;
import static org.openstreetmap.josm.plugins.mdbuildings.PostCheckUtils.findUncommonTags;
import org.openstreetmap.josm.tools.I18n;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.stream.Collectors;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.mdbuildings.ImportStatus;
import org.openstreetmap.josm.plugins.mdbuildings.gui.LifecyclePrefixBuildingTagDialog;
import org.openstreetmap.josm.plugins.mdbuildings.gui.UncommonTagDialog;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsImportData;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsImportStats;
import org.openstreetmap.josm.tools.Logging;

public class BuildingsImportAction extends JosmAction {
    static final String DESCRIPTION = I18n.tr(
        "Import the building at the location pointed to by the cursor and/or replace/update the selected one."
    );
    static final String TITLE = "MD-Buildings V2: " + I18n.tr("Download building");
    static final BuildingsImportStats importStats = BuildingsImportStats.getInstance();

    public BuildingsImportAction() {
        super(
            TITLE,
            "mdbuildings",
            DESCRIPTION,
            null,
            false,
            org.openstreetmap.josm.plugins.mdbuildings.BuildingsPlugin.getPluginInfo().getName() + ":buildings_import",
            false
        );
    }

    public static LatLon getCurrentCursorLocation() {
        if (MainApplication.getMap() != null && MainApplication.getMap().mapView != null
            && MainApplication.getMap().mapView.getMousePosition() != null) {
            return MainApplication.getMap().mapView.getLatLon(
                MainApplication.getMap().mapView.getMousePosition().getX(),
                MainApplication.getMap().mapView.getMousePosition().getY()
            );
        }
        return null;
    }

    /**
     * @param ds the dataset to check
     * @return – selected way in given dataset or null
     */
    public static Way getSelectedWay(DataSet ds) {
        if (ds == null) {
            return null;
        }
        Collection<Way> selected = ds.getSelectedWays();
        return selected.size() == 1 ? selected.iterator().next() : null;
    }

    public static Bounds getUserFrameViewBounds() {
        MapFrame mapFrame = MainApplication.getMap();
        if (mapFrame == null) { // tests
            return null;

        }
        return mapFrame.mapView.getState().getViewArea().getLatLonBoundsBox();
    }

    public static boolean showDialogIfFoundUncommonTags(Way resultBuilding, BuildingsImportManager manager) {
        if (resultBuilding == null) {
            return false;
        }

        TagMap uncommon = findUncommonTags(resultBuilding);
        if (uncommon.isEmpty()) {
            return false;
        }

        Logging.debug("Found uncommon tags {0}", uncommon);
        manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
        String tagsString = uncommon.getTags().stream()
                .map(t -> t.getKey() + "=" + t.getValue())
                .collect(Collectors.joining(", "));
        UncommonTagDialog.show(tagsString);
        return true;
    }

    public static void showDialogIfFoundLifecycleBuildingPrefixedTags(
        Way resultBuilding, BuildingsImportManager manager
    ) {
        if (resultBuilding == null) {
            return;
        }

        TagMap lifecyclePrefixBuildingTags = findLifecyclePrefixBuildingTags(resultBuilding);
        if (lifecyclePrefixBuildingTags.isEmpty()) {
            return;
        }

        Logging.debug("Found tags with life cycle prefixes {0}", lifecyclePrefixBuildingTags);
        manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
        String tagsString = lifecyclePrefixBuildingTags.getTags().stream()
                .map(t -> t.getKey() + "=" + t.getValue())
                .collect(Collectors.joining(", "));
        LifecyclePrefixBuildingTagDialog.show(tagsString);
    }

    public static void performBuildingImport(BuildingsImportManager manager) {
        importStats.addTotalImportActionCounter(1);

        BuildingsImportData buildingsImportData = manager.getImportedData();
        if (buildingsImportData == null) {  // Some error at importing data
            return;
        }

        if (buildingsImportData.isOutOfUserFrameView(getUserFrameViewBounds())) {
            Logging.warn("Imported building data outside the user's view.");
            manager.setStatus(ImportStatus.IMPORT_ERROR, I18n.tr("Imported building data outside the user's view."));
            return;
        }

        Way importedBuilding = (Way) manager.getNearestImportedBuilding(
            buildingsImportData,
            manager.getCurrentProfile(),
            manager.getCursorLatLon()
        );
        if (importedBuilding == null) {
            Logging.info("Building not found.");
            manager.setStatus(ImportStatus.NO_DATA, I18n.tr("Building not found."));
            return;
        }
        // Add importedBuilding to DataSet to prevent DataIntegrityError (primitives without osm metadata)
        new DataSet().addPrimitiveRecursive(importedBuilding);

        ImportStrategy importStrategy;
        switch (BuildingsSettings.IMPORT_MODE.get()) {
            case FULL:
                importStrategy = new FullImportStrategy(manager, importStats, importedBuilding);
                break;
            case GEOMETRY:
                importStrategy = new GeometryUpdateStrategy(manager, importStats, importedBuilding);
                break;
            case TAGS:
                importStrategy = new TagsUpdateStrategy(manager, importStats, importedBuilding);
                break;
            default:
                importStrategy = null;
                break;
        }

        if (importStrategy == null) {
            Logging.error("Incorrect import mode: " + BuildingsSettings.IMPORT_MODE.get());
            manager.setStatus(ImportStatus.IMPORT_ERROR, I18n.tr("Incorrect import mode."));
            return;
        }

        try {
            Way resultBuilding = importStrategy.performImport();
            manager.setResultBuilding(resultBuilding);

            boolean hasUncommonTags = BuildingsSettings.UNCOMMON_TAGS_CHECK.get()
                && showDialogIfFoundUncommonTags(resultBuilding, manager);
            showDialogIfFoundLifecycleBuildingPrefixedTags(resultBuilding, manager);
            manager.setStatus(ImportStatus.DONE, null);
            manager.updateGuiTags(hasUncommonTags);
            
            if (BuildingsSettings.MERGE_INTERIOR_ADDRESS_NODES.get() && resultBuilding != null) {
                MergeAddressNodesCommand mergeCmd = new MergeAddressNodesCommand(
                        manager.getEditLayer(), resultBuilding);
                boolean merged = mergeCmd.executeCommand();
                if (merged) {
                    UndoRedoHandler.getInstance().add(mergeCmd, false);
                }
            }
        } catch (ImportActionCanceledException exception) {
            Logging.info("{0} {1}", exception.getStatus(), exception.getMessage());
            manager.setStatus(exception.getStatus(), exception.getMessage());
        } finally {
            manager.getEditLayer().clearSelection();
            importStats.save();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        actionPerformed(actionEvent, null);
    }

    /**
     * Perform building import with an explicitly pre-captured selected building.
     * This overload exists to support the Ctrl+Double Click mouse trigger, where
     * the selected way must be captured at mousePressed time — before JOSM's own
     * click handlers potentially change/clear the selection when the double-click fires.
     *
     * @param actionEvent the action event (may be null for mouse triggers)
     * @param preselectedBuilding the building to treat as selected, or null to read from dataset selection
     */
    public void actionPerformed(ActionEvent actionEvent, Way preselectedBuilding) {
        DataSet currentDataSet = getLayerManager().getEditDataSet();

        // Get selection - preselectedBuilding takes priority (captured at mouse‐press time
        // before the double‐click event may have changed/cleared the JOSM selection)
        Way selectedBuilding = preselectedBuilding != null
            ? preselectedBuilding
            : getSelectedWay(currentDataSet);
        LatLon cursorLatLon = getCurrentCursorLocation();

        BuildingsImportManager buildingsImportManager = new BuildingsImportManager(
            currentDataSet,
            cursorLatLon,
            selectedBuilding
        );
        try {
            buildingsImportManager.validate();
        } catch (ImportActionCanceledException exception) {
            Logging.info("{0} {1}", exception.getStatus(), exception.getMessage());
            buildingsImportManager.setStatus(exception.getStatus(), exception.getMessage());
            return;
        }

        buildingsImportManager.run();
    }
}
