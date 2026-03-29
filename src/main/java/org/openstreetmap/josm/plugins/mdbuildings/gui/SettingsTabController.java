package org.openstreetmap.josm.plugins.mdbuildings.gui;

import java.awt.Component;

public interface SettingsTabController {
    String getTabTitle();

    Component getTabView();
}
