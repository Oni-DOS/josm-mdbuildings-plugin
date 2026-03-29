package org.openstreetmap.josm.plugins.mdbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;

public class BuildingsPreferenceSetting extends DefaultTabPreferenceSetting {

    private final SettingsController settingsController;

    public BuildingsPreferenceSetting(SettingsController settingsController) {
        super("mdbuildings", tr("MD-Buildings V2"), tr("Settings and statistics for MD-Buildings V2 plugin."),
                false, new JTabbedPane());
        this.settingsController = settingsController;
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        // Build the content: two tabs - Settings and Statistics
        JTabbedPane outerTabs = new JTabbedPane();

        // --- Settings tab: a nested JTabbedPane with all setting controllers ---
        JTabbedPane settingsTabs = new JTabbedPane();
        for (SettingsTabController controller : settingsController.getSettingsTabControllers()) {
            if (!(controller instanceof SettingsStatsController)) {
                settingsTabs.addTab(controller.getTabTitle(), controller.getTabView());
            }
        }
        outerTabs.addTab(tr("Settings"), settingsTabs);

        // --- Statistics tab ---
        for (SettingsTabController controller : settingsController.getSettingsTabControllers()) {
            if (controller instanceof SettingsStatsController) {
                outerTabs.addTab(tr("Statistics"), controller.getTabView());
                break;
            }
        }

        // Wrap in a panel and use the standard JOSM method to embed it in the Preferences window
        JPanel content = new JPanel(new BorderLayout());
        content.add(outerTabs, BorderLayout.CENTER);
        createPreferenceTabWithScrollPane(gui, content);
    }

    @Override
    public javax.swing.ImageIcon getIcon(org.openstreetmap.josm.tools.ImageProvider.ImageSizes size) {
        return org.openstreetmap.josm.tools.ImageProvider.get("mdbuildings");
    }

    @Override
    public boolean ok() {
        return true;
    }
}
