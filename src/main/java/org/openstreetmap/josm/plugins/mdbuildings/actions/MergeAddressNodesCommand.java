package org.openstreetmap.josm.plugins.mdbuildings.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Geometry;

public class MergeAddressNodesCommand extends Command {

    private final Way building;
    private SequenceCommand internalCommand;

    public MergeAddressNodesCommand(DataSet dataSet, Way building) {
        super(dataSet);
        this.building = building;
    }

    @Override
    public boolean executeCommand() {
        if (internalCommand == null) {
            List<Command> commands = new ArrayList<>();
            List<Node> interiorNodes = new ArrayList<>();

            DataSet dataSet = building.getDataSet();
            if (dataSet == null) {
                return false;
            }

            // Find nodes visually inside the polygon using Geometry.nodeInsidePolygon
            for (Node n : dataSet.getNodes()) {
                if (!n.isDeleted() && !building.getNodes().contains(n) 
                        && Geometry.nodeInsidePolygon(n, building.getNodes())) {
                    interiorNodes.add(n);
                }
            }

            // Process interior nodes
            List<OsmPrimitive> nodesToDelete = new ArrayList<>();
            for (Node n : interiorNodes) {
                boolean hasAddrTags = false;
                boolean hasOtherFunctionalTags = false;

                Map<String, String> keys = n.getKeys();
                for (Map.Entry<String, String> entry : keys.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("addr:")) {
                        hasAddrTags = true;
                        // Copy addr tag to building if not present or we want to overwrite? Let's assume we copy.
                        // We will queue a property change for the building.
                        commands.add(new ChangePropertyCommand(building, key, entry.getValue()));
                    } else if (!key.equals("source") && !key.equals("created_by")) {
                        // Any tag other than addr:*, source, or created_by is considered functional
                        hasOtherFunctionalTags = true;
                    }
                }

                if (hasAddrTags && !hasOtherFunctionalTags) {
                    nodesToDelete.add(n);
                }
            }

            if (!nodesToDelete.isEmpty()) {
                commands.add(new DeleteCommand(nodesToDelete));
            }

            if (commands.isEmpty()) {
                return false; // Nothing to do
            }

            internalCommand = new SequenceCommand(tr("Merge interior address nodes"), commands);
        }

        return internalCommand.executeCommand();
    }

    @Override
    public void undoCommand() {
        if (internalCommand != null) {
            internalCommand.undoCommand();
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, 
                                 Collection<OsmPrimitive> added) {
        if (internalCommand != null) {
            internalCommand.fillModifiedData(modified, deleted, added);
        }
    }

    @Override
    public String getDescriptionText() {
        return tr("Merge interior address nodes");
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        if (internalCommand != null) {
            return internalCommand.getParticipatingPrimitives();
        }
        return Collections.emptyList();
    }
}
