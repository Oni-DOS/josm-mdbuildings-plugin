package org.openstreetmap.josm.plugins.mdbuildings;
import org.openstreetmap.josm.plugins.mdbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsAutoremoveSourceTagsController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsDataSourcesController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsNotificationsController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsStatsController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsTabController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsUncommonTagsController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.ToggleDialogController;
import org.openstreetmap.josm.plugins.mdbuildings.gui.BuildingsPreferenceSetting;
import org.openstreetmap.josm.plugins.mdbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsAutoremoveSourceTagsPanel;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsDataSourcesPanel;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsNotificationsPanel;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsStatsPanel;
import org.openstreetmap.josm.plugins.mdbuildings.gui.SettingsUncommonTagsPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;

public class BuildingsPlugin extends Plugin {
    private static org.openstreetmap.josm.plugins.PluginInformation info;
    public static org.openstreetmap.josm.plugins.PluginInformation getPluginInfo() {
        return info;
    }
    protected static ToggleDialogController toggleDialogController;
    private final SettingsController settingsController;
    private final BuildingsImportAction buildingsImportAction;

    public BuildingsPlugin(org.openstreetmap.josm.plugins.PluginInformation info) {
        super(info);
        BuildingsPlugin.info = info;

        DataSourceConfig dataSourceConfig = new DataSourceConfig();

        List<SettingsTabController> settingsTabControllers = List.of(
            new SettingsDataSourcesController(dataSourceConfig, new SettingsDataSourcesPanel()),
            new SettingsStatsController(new SettingsStatsPanel()),
            new SettingsNotificationsController(new NotificationConfig(), new SettingsNotificationsPanel()),
            new SettingsUncommonTagsController(
                new TagValues(BuildingsSettings.COMMON_BUILDING_TAGS), new SettingsUncommonTagsPanel()
            ),
            new SettingsAutoremoveSourceTagsController(
                new TagValues(BuildingsSettings.UNWANTED_SOURCE_VALUES), new SettingsAutoremoveSourceTagsPanel()
            )
        );
        this.settingsController = new SettingsController(settingsTabControllers);

        this.buildingsImportAction = new BuildingsImportAction();
        MainMenu.add(MainApplication.getMenu().selectionMenu, buildingsImportAction);
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new BuildingsPreferenceSetting(settingsController);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        if (newFrame != null) {
            BuildingsToggleDialog toggleDialog = new BuildingsToggleDialog();
            toggleDialogController = new ToggleDialogController(new DataSourceConfig(), toggleDialog);
            newFrame.addToggleDialog(toggleDialog);

            newFrame.mapView.addMouseListener(new MouseAdapter() {
                // Capture the selection at press-time, before JOSM's own click listeners
                // process the event and potentially change/clear the selection.
                // A double-click fires: mousePressed → mouseReleased → mouseClicked (×1)
                //                       mousePressed → mouseReleased → mouseClicked (×2)
                // By the time mouseClicked with clickCount==2 fires, the first single-click
                // may already have changed the dataset selection, so we must snapshot it here.
                private Way pressedSelectedBuilding = null;

                @Override
                public void mousePressed(MouseEvent e) {
                    if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
                        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
                        LatLon latLon = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
                        pressedSelectedBuilding = ds != null
                            ? org.openstreetmap.josm.plugins.mdbuildings.NearestBuilding.getBuildingAt(ds, latLon)
                            : null;
                    } else {
                        pressedSelectedBuilding = null;
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
                        buildingsImportAction.actionPerformed(null, pressedSelectedBuilding);
                    }
                }
            });
        } else {
            toggleDialogController = null;
        }
    }
}
