package org.openstreetmap.josm.plugins.mdbuildings.gui;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {

    private final List<SettingsTabController> settingsTabControllers;

    public SettingsController(List<SettingsTabController> controllers) {
        this.settingsTabControllers = new ArrayList<>(controllers);
    }

    public List<SettingsTabController> getSettingsTabControllers() {
        return settingsTabControllers;
    }
}
