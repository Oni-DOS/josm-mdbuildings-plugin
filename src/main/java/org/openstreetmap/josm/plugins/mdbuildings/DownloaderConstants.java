package org.openstreetmap.josm.plugins.mdbuildings;

import org.openstreetmap.josm.data.Version;

public final class DownloaderConstants {
    public static final String USER_AGENT = String.format(
        "%s/%s %s",
        BuildingsPlugin.getPluginInfo().name,
        BuildingsPlugin.getPluginInfo().localversion,
        Version.getInstance().getFullAgentString()
    );

    private DownloaderConstants() {
    }
}
