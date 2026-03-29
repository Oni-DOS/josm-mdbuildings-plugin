package org.openstreetmap.josm.plugins.mdbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.util.LinkedHashMap;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsImportStats;

public class SettingsStatsController implements SettingsTabController {

    private final SettingsStatsPanel view;

    public SettingsStatsController(SettingsStatsPanel view) {
        this.view = view;
        updateStats();
    }

    public void updateStats() {
        BuildingsImportStats buildingsStats = BuildingsImportStats.getInstance();
        LinkedHashMap<String, String> stats = new LinkedHashMap<>();
        stats.put(tr("New buildings"), Integer.toString(buildingsStats.getImportNewBuildingCounter()));
        stats.put(tr("Imports with building replace"), Integer.toString(buildingsStats.getImportWithReplaceCounter()));
        stats.put(tr("Imports with tags update"), Integer.toString(buildingsStats.getImportWithTagsUpdateCounter()));
        stats.put(tr("Imports with geometry update"),
                Integer.toString(buildingsStats.getImportWithGeometryUpdateCounter()));
        stats.put(tr("Total import actions"), Integer.toString(buildingsStats.getTotalImportActionCounter()));
        
        view.updateStats(stats);
    }

    @Override
    public String getTabTitle() {
        return tr("Stats");
    }

    @Override
    public Component getTabView() {
        updateStats(); // Refresh when tab is accessed
        return view;
    }
}
