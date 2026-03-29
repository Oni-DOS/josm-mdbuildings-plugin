package org.openstreetmap.josm.plugins.mdbuildings;
import org.openstreetmap.josm.plugins.mdbuildings.actions.ImportActionCanceledException;
import static org.openstreetmap.josm.plugins.mdbuildings.actions.BuildingsImportAction.performBuildingImport;
import static org.openstreetmap.josm.plugins.mdbuildings.CombineNearestOneDsStrategy.ACCEPT;
import static org.openstreetmap.josm.plugins.mdbuildings.CombineNearestOneDsStrategy.ASK_USER;
import static org.openstreetmap.josm.plugins.mdbuildings.CombineNearestOneDsStrategy.CANCEL;
import static org.openstreetmap.josm.plugins.mdbuildings.ImportStatus.DOWNLOADING;
import static org.openstreetmap.josm.plugins.mdbuildings.ImportStatus.IDLE;
import static org.openstreetmap.josm.plugins.mdbuildings.gui.NotificationPopup.showNotification;
import static org.openstreetmap.josm.plugins.mdbuildings.NearestBuilding.getNearestBuilding;
import static org.openstreetmap.josm.plugins.mdbuildings.PreCheckUtils.validateSelectedWay;
import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.mdbuildings.gui.ImportedBuildingOneDsOptionDialog;
import org.openstreetmap.josm.plugins.mdbuildings.gui.ImportedBuildingOverlappingOptionDialog;


/**
 * Responsible for managing all import action workers, data sources, data, actions, GUI.
 */
public class BuildingsImportManager {
    private final LatLon cursorLatLon;
    private final Way selectedBuilding;
    private final DataSet editLayer;
    private final DataSourceConfig dataSourceConfig;
    private DataSourceProfile currentProfile;
    private BuildingsImportData importedData;
    private ImportStatus status;
    private Way resultBuilding;

    private final NotificationConfig notificationConfig;

    public BuildingsImportManager(DataSet editLayer, LatLon cursorLatLon, Way selectedBuilding) {
        this.editLayer = editLayer;
        this.cursorLatLon = cursorLatLon;
        this.selectedBuilding = selectedBuilding;
        this.dataSourceConfig = new DataSourceConfig();
        this.currentProfile = dataSourceConfig.getCurrentProfile();
        this.notificationConfig = new NotificationConfig();

        this.importedData = null;
        this.resultBuilding = null;
        this.status = IDLE;
    }

    public BuildingsImportData getImportedData() {
        return importedData;
    }

    public LatLon getCursorLatLon() {
        return cursorLatLon;
    }

    public Way getSelectedBuilding() {
        return selectedBuilding;
    }

    public DataSet getEditLayer() {
        return editLayer;
    }

    public DataSourceProfile getCurrentProfile() {
        return this.currentProfile;
    }

    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setImportedData(BuildingsImportData importedData) {
        this.importedData = importedData;
    }

    public void setCurrentProfile(DataSourceProfile currentProfile) {
        this.currentProfile = currentProfile;
    }

    public void setResultBuilding(Way resultBuilding) {
        this.resultBuilding = resultBuilding;
    }

    public void setStatus(ImportStatus newStatus, String reason) {
        this.status = newStatus;
        updateGuiStatus();
        Notification notification = Notification.fromImportStatus(status);
        if (notification != null && notificationConfig.isNotificationEnabled(notification)) {
            showNotification(status + ": " + reason);
        }
    }

    public void validate() throws ImportActionCanceledException {
        if (cursorLatLon == null) {
            throw new ImportActionCanceledException(
                tr("Cursor outside the map view!"), ImportStatus.IMPORT_ERROR
            );
        }
        if (currentProfile == null) {
            throw new ImportActionCanceledException(
                tr("No data source profile selected!"), ImportStatus.IMPORT_ERROR
            );
        }
        validateSelectedWay(selectedBuilding);
    }

    public void run() {
        BuildingsDownloadTask task = new BuildingsDownloadTask(this);
        setStatus(DOWNLOADING, null);
        task.execute();
    }

    public void processDownloadedData() {
        performBuildingImport(this);
    }

    private void updateGuiStatus() {
        // for tests and no-gui execution of method
        if (BuildingsPlugin.toggleDialogController == null) {
            return;
        }

        boolean autoChangeToDefault;
        switch (status) {
            case IDLE:
            case DOWNLOADING:
            case ACTION_REQUIRED:
                autoChangeToDefault = false;
                break;
            case DONE:
            case NO_DATA:
            case NO_UPDATE:
            case CANCELED:
            case CONNECTION_ERROR:
            case IMPORT_ERROR:
            default: // DONE, NO_DATA, NO_UPDATE
                autoChangeToDefault = true;
        }
        BuildingsPlugin.toggleDialogController.setStatus(status, autoChangeToDefault);
    }

    /**
     * Helper function to updating GUI latest tags from action
     */
    public void updateGuiTags(boolean hasUncommonTags) {
        // for tests and no-gui execution of method
        if (BuildingsPlugin.toggleDialogController == null) {
            return;
        }

        String buildingText = "";
        String buildingLevelsText = "";
        if (resultBuilding != null) {
            buildingText = resultBuilding.getKeys().getOrDefault("building", "");
            buildingLevelsText = resultBuilding.getKeys().getOrDefault("building:levels", "");
        }
        BuildingsPlugin.toggleDialogController.updateTags(buildingText, buildingLevelsText, hasUncommonTags);
    }

    static CombineNearestOneDsStrategy getImportBuildingDataOneDsStrategy(ImportedBuildingOneDsOptionDialog dialog) {
        CombineNearestOneDsStrategy strategy = CombineNearestOneDsStrategy.fromString(
            BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.get()
        );
        if (BuildingsSessionStateManager.getOneDsConfirmationSessionStrategy() != null) {
            return BuildingsSessionStateManager.getOneDsConfirmationSessionStrategy();
        }
        if (strategy == ASK_USER) {
            dialog.show();
            strategy = dialog.isUserConfirmedOneDs() ? ACCEPT : CANCEL;
            if (dialog.isDoNotShowAgainThisSession()) {
                BuildingsSessionStateManager.setOneDsConfirmationSessionStrategy(strategy);
            }
        }
        return strategy;
    }

    boolean shouldShowOneDsNotification() {
        CombineNearestOneDsStrategy settingStrategy = CombineNearestOneDsStrategy.fromString(
            BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.get()
        );
        if (settingStrategy == ASK_USER && BuildingsSessionStateManager.getOneDsConfirmationSessionStrategy() == null) {
            return false;
        }
        return notificationConfig.isNotificationEnabled(Notification.ONE_DS_MISSING);
    }

    static CombineNearestOverlappingStrategy getImportBuildingOverlappingStrategy(
        ImportedBuildingOverlappingOptionDialog dialog
    ) {
        if (BuildingsSessionStateManager.getOverlappingConfirmationSessionStrategy() != null) {
            return BuildingsSessionStateManager.getOverlappingConfirmationSessionStrategy();
        }

        dialog.show();
        CombineNearestOverlappingStrategy strategy = dialog.getUserConfirmedStrategy();
        if (dialog.isDoNotShowAgainThisSession()) {
            BuildingsSessionStateManager.setOverlappingConfirmationSessionStrategy(strategy);
        }
        return strategy;
    }

    boolean shouldShowNotEnoughOverlappingNotification() {
        if (BuildingsSessionStateManager.getOverlappingConfirmationSessionStrategy() == null) {
            return false;
        }
        return notificationConfig.isNotificationEnabled(Notification.NOT_ENOUGH_OVERLAPPING);
    }

    public static void injectSourceTags(OsmPrimitive importedBuilding, String geometrySource, String tagsSource) {
        // Use the customizable default source tag from settings
        importedBuilding.put("source", BuildingsSettings.DEFAULT_SOURCE.get());
        importedBuilding.remove("source:building");
        importedBuilding.remove("source:geometry");
    }

    /**
     * Create a new building based on provided parameters. It's cloned with new id/nodes.
     *
     * @param geometryBuilding – building from which only geometry will be reused
     * @param tagsBuilding     – building from which only tags will be reused
     */
    static Way combineBuildings(Way geometryBuilding, Way tagsBuilding) {
        Way newBuilding = new Way();
        newBuilding.setNodes(geometryBuilding.getNodes());
        newBuilding.setKeys(tagsBuilding.getKeys());

        return newBuilding;
    }

    /**
     * Get the nearest building object from 1-2 downloaded data sources as 1 building ready to import
     * It will use default strategies from settings for all problematic cases or ask user (GUI).
     * Cases:
     * .
     * 1. Same data source:
     * a) empty dataset -> return null
     * b) good dataset -> return nearest building
     * .
     * 2. Different data sources:
     * a) empty both data sets -> return null
     * b) empty one data set (default strategy or ask user)
     * -- user allows to use one data source -> return building based on 1 data source
     * -- user doesn't allow -> return null
     * c) both datasets with data:
     * -- buildings overlap with threshold e.g. 70% (setting) – merging both into 1 -> return combined building
     * -- buildings don’t overlap (default strategy or ask user):
     * ---- user allows to combine if not overlap - merging both into 1 -> return combined building
     * ---- user pick "tags" data source -> return based on tags data source
     * ---- user pick "geometry" data source -> return based on geometry data source
     * ---- user doesn't allow -> return null
     *
     * @param importedData – should be matched to profile parameter and contain 1 or 2 data sources with buildings data
     * @param profile      – should be matched with importData parameter
     * @param latLon       – cursor/start point location which is used to get the nearest building
     * @return building or null if it couldn't combine building or datasets empty/user decision/settings etc.
     * @throws NullPointerException if dataSourceProfile is not set
     */
    public OsmPrimitive getNearestImportedBuilding(
        BuildingsImportData importData,
        DataSourceProfile profile,
        LatLon latLon
    ) {
        OsmPrimitive importedBuilding;

        String importedBuildingGeometrySource = profile.getGeometry();
        String importedBuildingTagsSource = profile.getTags();

        // One data source
        if (profile.isOneDataSource()) {
            importedBuilding = getNearestBuilding(importData.get(profile.getGeometry()), latLon);
            importedBuildingGeometrySource = profile.getGeometry();
            importedBuildingTagsSource = profile.getGeometry();
        } else {
            DataSet geometryDs = importData.get(profile.getGeometry());
            DataSet tagsDs = importData.get(profile.getTags());

            // Both empty
            if (geometryDs.isEmpty() && tagsDs.isEmpty()) {
                importedBuilding = null;
            } else if (geometryDs.isEmpty() != tagsDs.isEmpty()) {
                String availableDsName = geometryDs.isEmpty() ? profile.getTags() : profile.getGeometry();

                String notificationText;
                ImportedBuildingOneDsOptionDialog oneDsDialog = new ImportedBuildingOneDsOptionDialog(availableDsName);
                if (getImportBuildingDataOneDsStrategy(oneDsDialog) == ACCEPT) {
                    importedBuilding = NearestBuilding.getNearestBuilding(importData.get(availableDsName), latLon);
                    importedBuildingTagsSource = availableDsName;
                    importedBuildingGeometrySource = availableDsName;
                    notificationText = tr("One data source is missing. Used") + ": " + availableDsName;
                } else {
                    importedBuilding = null;
                    notificationText = tr("One data source is missing. Canceling.");
                }
                if (shouldShowOneDsNotification()) {
                    showNotification(notificationText);
                }
            } else {
                Way geometryBuilding = !geometryDs.getWays().isEmpty() ? geometryDs.getWays().iterator().next() : null;
                Way tagsBuilding = !tagsDs.getWays().isEmpty() ? tagsDs.getWays().iterator().next() : null;

                if (geometryBuilding == null || tagsBuilding == null) {
                    importedBuilding = null;
                    String notificationText = tr("One data source is missing. Canceling.");
                    if (shouldShowOneDsNotification()) {
                        showNotification(notificationText);
                    }
                } else {
                    double overlapPercentage = BuildingsOverlapDetector.detect(geometryBuilding, tagsBuilding);

                if (overlapPercentage
                    >= BuildingsSettings.COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD.get()) {
                    importedBuilding = combineBuildings(geometryBuilding, tagsBuilding);
                } else {
                    String notificationText = tr(
                        "Imported building data does not overlapping enough. Used strategy"
                    ) + ": ";
                    ImportedBuildingOverlappingOptionDialog overlappingDialog =
                        new ImportedBuildingOverlappingOptionDialog(
                            profile.getGeometry(),
                            profile.getTags(),
                            overlapPercentage
                        );
                    CombineNearestOverlappingStrategy strategy = getImportBuildingOverlappingStrategy(
                        overlappingDialog
                    );

                    switch (strategy) {
                        case MERGE_BOTH:
                            importedBuilding = combineBuildings(geometryBuilding, tagsBuilding);
                            notificationText += tr("Merged both");
                            break;
                        case ACCEPT_GEOMETRY_SOURCE:
                            importedBuilding = geometryBuilding;
                            importedBuildingGeometrySource = profile.getGeometry();
                            importedBuildingTagsSource = profile.getGeometry();
                            notificationText += tr("Used {0}", profile.getGeometry());
                            break;
                        case ACCEPT_TAGS_SOURCE:
                            importedBuilding = tagsBuilding;
                            importedBuildingGeometrySource = profile.getTags();
                            importedBuildingTagsSource = profile.getTags();
                            notificationText += tr("Used {0}", profile.getTags());
                            break;
                        default:
                            importedBuilding = null;
                            notificationText = tr("Imported building data does not overlapping enough. Canceling.");
                            break;
                    }
                    if (shouldShowNotEnoughOverlappingNotification()) {
                        showNotification(notificationText);
                    }
                    }
                }
            }
        }
        if (importedBuilding != null) {
            injectSourceTags(importedBuilding, importedBuildingGeometrySource, importedBuildingTagsSource);
        }
        return CloneBuilding.cloneBuilding(importedBuilding);
    }
}
