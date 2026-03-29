package org.openstreetmap.josm.plugins.mdbuildings.gui;

import static org.openstreetmap.josm.plugins.mdbuildings.SettingsDataSourcesProfilesTableModel.COL_PROFILE;
import static org.openstreetmap.josm.plugins.mdbuildings.SettingsDataSourcesProfilesTableModel.COL_SERVER;
import static org.openstreetmap.josm.plugins.mdbuildings.SettingsDataSourcesProfilesTableModel.COL_VISIBLE;
import static org.openstreetmap.josm.plugins.mdbuildings.SettingsDataSourcesProfilesTableModel.PROFILE_COLUMNS;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.plugins.mdbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.mdbuildings.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.mdbuildings.DataSourceConfig;
import org.openstreetmap.josm.plugins.mdbuildings.DataSourceProfile;
import org.openstreetmap.josm.plugins.mdbuildings.DataSourceServer;
import org.openstreetmap.josm.plugins.mdbuildings.SettingsDataSourcesProfilesTableModel;
import org.openstreetmap.josm.plugins.mdbuildings.SettingsDataSourcesServersListModel;
import org.openstreetmap.josm.plugins.mdbuildings.SettingsImportOneDsStrategyComboBoxModel;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.OpenBrowser;

public final class SettingsDataSourcesController implements SettingsTabController {

    private final DataSourceConfig dataSourceConfigModel;
    private final SettingsDataSourcesPanel settingsDataSourcesPanelView;

    private final SettingsDataSourcesProfilesTableModel profilesTableModel;
    private final SettingsDataSourcesServersListModel serversListModel;
    private final SettingsImportOneDsStrategyComboBoxModel importOneDsStrategyComboBoxModel;

    public SettingsDataSourcesController(DataSourceConfig dataSourceConfig,
                                         SettingsDataSourcesPanel settingsDataSourcesPanelView) {
        this.dataSourceConfigModel = dataSourceConfig;
        this.settingsDataSourcesPanelView = settingsDataSourcesPanelView;
        this.profilesTableModel = new SettingsDataSourcesProfilesTableModel();
        this.serversListModel = new SettingsDataSourcesServersListModel();
        this.importOneDsStrategyComboBoxModel = new SettingsImportOneDsStrategyComboBoxModel();

        settingsDataSourcesPanelView.setProfilesTableModel(profilesTableModel);
        settingsDataSourcesPanelView.setServersListModel(serversListModel);
        settingsDataSourcesPanelView.setImportOneDsStrategyComboBoxModel(importOneDsStrategyComboBoxModel);

        initViewListeners();
        initModelListeners();

        updateServerList();
        updateProfilesTable();
        updateImportOneDsStrategyComboBox();

        settingsDataSourcesPanelView.setUseDtmElevationCheckBoxSelected(
            BuildingsSettings.USE_DTM_ELEVATION.get()
        );
        settingsDataSourcesPanelView.setMergeInteriorAddressNodesCheckBoxSelected(
            BuildingsSettings.MERGE_INTERIOR_ADDRESS_NODES.get()
        );
        settingsDataSourcesPanelView.setDefaultSourceFieldText(
            BuildingsSettings.DEFAULT_SOURCE.get()
        );
    }

    private void initModelListeners() {
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.PROFILES, evt -> updateProfilesTable());
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.SERVERS, evt -> updateServerList());
    }

    private void initViewListeners() {
        settingsDataSourcesPanelView.upBtnAddActionListener(actionEvent -> {
            settingsDataSourcesPanelView.upBtnSetEnabled(false);
            moveProfileUp();
        });

        settingsDataSourcesPanelView.downBtnAddActionListener(actionEvent -> {
            settingsDataSourcesPanelView.downBtnSetEnabled(false);
            moveProfileDown();
        });

        settingsDataSourcesPanelView.downBtnAddActionListener(actionEvent -> {
            settingsDataSourcesPanelView.downBtnSetEnabled(false);
            moveProfileDown();
        });

        profilesTableModel.addTableModelListener((tableModelEvent -> {
            int row = tableModelEvent.getFirstRow();
            int column = tableModelEvent.getColumn();

            if (column == PROFILE_COLUMNS.indexOf(COL_VISIBLE)) {
                SettingsDataSourcesProfilesTableModel model =
                    (SettingsDataSourcesProfilesTableModel) tableModelEvent.getSource();
                Boolean checked = (Boolean) model.getValueAt(row, column);

                String serverName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_SERVER));
                String profileName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_PROFILE));
                DataSourceProfile dataSourceProfile = dataSourceConfigModel.getProfileByName(serverName, profileName);
                dataSourceConfigModel.setProfileVisible(dataSourceProfile, checked);
            }
        }));

        settingsDataSourcesPanelView.profilesTableAddListSelectionListener((listSelectionEvent) -> {
            int index = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();

            boolean isSelected = index != -1;
            settingsDataSourcesPanelView.editProfileBtnSetEnabled(isSelected);
            settingsDataSourcesPanelView.removeProfileBtnSetEnabled(isSelected);

            if (index == 0) {
                settingsDataSourcesPanelView.upBtnSetEnabled(false);
                settingsDataSourcesPanelView.downBtnSetEnabled(true);
            } else if (index == settingsDataSourcesPanelView.getProfilesTableRowCount() - 1) {
                settingsDataSourcesPanelView.upBtnSetEnabled(true);
                settingsDataSourcesPanelView.downBtnSetEnabled(false);
            } else if (!isSelected) { // no selection
                settingsDataSourcesPanelView.upBtnSetEnabled(false);
                settingsDataSourcesPanelView.downBtnSetEnabled(false);
            } else {
                settingsDataSourcesPanelView.upBtnSetEnabled(true);
                settingsDataSourcesPanelView.downBtnSetEnabled(true);
            }
        });

        settingsDataSourcesPanelView.serversListAddListSelectionListener(
            listSelectionEvent -> {
                int index = settingsDataSourcesPanelView.getServerListSelectedIndex();
                settingsDataSourcesPanelView.editServerBtnSetEnabled(index != -1);
                settingsDataSourcesPanelView.removeServerBtnSetEnabled(index != -1);
            }
        );

        settingsDataSourcesPanelView.addServerBtnAddActionListener(actionEvent -> addServerAction());
        settingsDataSourcesPanelView.editServerBtnAddActionListener(actionEvent -> editServerAction());
        settingsDataSourcesPanelView.removeServerBtnAddActionListener(actionEvent -> removeServerAction());

        settingsDataSourcesPanelView.addProfileBtnAddActionListener(actionEvent -> addProfileAction());
        settingsDataSourcesPanelView.editProfileBtnAddActionListener(actionEvent -> editProfileAction());
        settingsDataSourcesPanelView.removeProfileBtnAddActionListener(actionEvent -> removeProfileAction());

        settingsDataSourcesPanelView.bulkEditBtnAddActionListener(actionEvent -> bulkEditAction());
        settingsDataSourcesPanelView.onlineHelpBtnAddActionListener(actionEvent -> {
            OpenBrowser.displayUrl("https://github.com/Oni-DOS/josm-mdbuildings-plugin/wiki");
        });

        settingsDataSourcesPanelView.addImportOneDsStrategyComboBoxItemListener(
            new ImportOneDsStrategyComboBoxItemChanged()
        );

        settingsDataSourcesPanelView.addMergeInteriorAddressNodesCheckBoxItemListener(
            itemEvent -> BuildingsSettings.MERGE_INTERIOR_ADDRESS_NODES.put(
                itemEvent.getStateChange() == ItemEvent.SELECTED
            )
        );

        settingsDataSourcesPanelView.addUseDtmElevationCheckBoxItemListener(
            itemEvent -> BuildingsSettings.USE_DTM_ELEVATION.put(
                itemEvent.getStateChange() == ItemEvent.SELECTED
            )
        );

        settingsDataSourcesPanelView.addDefaultSourceFieldDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            private void update() {
                BuildingsSettings.DEFAULT_SOURCE.put(settingsDataSourcesPanelView.getDefaultSourceFieldText());
            }
        });
    }

    private void updateServerList() {
        serversListModel.clear();
        dataSourceConfigModel.getServers().forEach(server -> serversListModel.addElement(
            String.format("%s: %s", server.getName(), server.getUrl())
        ));
    }

    private void updateProfilesTable() {
        settingsDataSourcesPanelView.profilesTableClearSelection();
        profilesTableModel.getDataVector().removeAllElements();
        dataSourceConfigModel.getProfiles().forEach(profile -> profilesTableModel.addRow(new Object[] {
            profile.getName(),
            profile.getDataSourceServerName(),
            profile.getTags(),
            profile.getGeometry(),
            profile.isVisible()
        }));
    }

    private void updateImportOneDsStrategyComboBox() {
        importOneDsStrategyComboBoxModel.removeAllElements();
        importOneDsStrategyComboBoxModel.addAll(List.of(CombineNearestOneDsStrategy.values()));
        importOneDsStrategyComboBoxModel.setSelectedItem(
            CombineNearestOneDsStrategy.fromString(BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.get())
        );
    }

    private void moveProfile(int srcRowIndex, int dstRowIndex) {
        int indexColServer = profilesTableModel.findColumn(COL_SERVER);
        int indexColProfile = profilesTableModel.findColumn(COL_PROFILE);

        DataSourceProfile srcProfile = dataSourceConfigModel.getProfileByName(
            (String) profilesTableModel.getValueAt(srcRowIndex, indexColServer),
            (String) profilesTableModel.getValueAt(srcRowIndex, indexColProfile)
        );
        DataSourceProfile dstProfile = dataSourceConfigModel.getProfileByName(
            (String) profilesTableModel.getValueAt(dstRowIndex, indexColServer),
            (String) profilesTableModel.getValueAt(dstRowIndex, indexColProfile)
        );
        dataSourceConfigModel.swapProfileOrder(srcProfile, dstProfile);
    }

    private void moveProfileUp() {
        int rowIndex = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (rowIndex == 0) {
            Logging.error("Trying to move up first profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex - 1);
    }

    private void moveProfileDown() {
        int rowIndex = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (rowIndex == settingsDataSourcesPanelView.getProfilesTableRowCount() - 1) {
            Logging.error("Trying to move down last profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex + 1);
    }

    private void addServerAction() {
        boolean success = settingsDataSourcesPanelView.promptNewServerNameUrl();
        if (!success) {
            return;
        }
        try {
            DataSourceServer newServer = new DataSourceServer(
                settingsDataSourcesPanelView.getAddServerNameFieldText(),
                settingsDataSourcesPanelView.getAddServerUrlFieldText()
            );
            dataSourceConfigModel.addServer(newServer);
        } catch (IllegalArgumentException exception) {
            settingsDataSourcesPanelView.showAddNewServerErrorDialog();
        }

    }

    private void editServerAction() {
        int index = settingsDataSourcesPanelView.getServerListSelectedIndex();
        if (index == -1) {
            return;
        }
        DataSourceServer server = dataSourceConfigModel.getServers().get(index);
        
        String newName = settingsDataSourcesPanelView.promptEditServerName(server.getName());
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }
        
        String newUrl = settingsDataSourcesPanelView.promptEditServerUrl(server.getUrl());
        if (newUrl == null || newUrl.trim().isEmpty()) {
            return;
        }
        
        DataSourceServer newServer = new DataSourceServer(newName, newUrl);
        dataSourceConfigModel.updateServer(server, newServer);
    }

    private void removeServerAction() {
        int serverIndex = settingsDataSourcesPanelView.getServerListSelectedIndex();
        DataSourceServer selectedServer = dataSourceConfigModel.getServers().get(serverIndex);

        boolean success = settingsDataSourcesPanelView.showRemoveServerConfirmDialog(selectedServer.getName());
        if (success) {
            dataSourceConfigModel.removeServer(selectedServer);
        }
    }

    private void addProfileAction() {
        ProfileEditDialog dialog = new ProfileEditDialog(dataSourceConfigModel.getServers(), null);
        if (dialog.showGui().getValue() == 1) {
            DataSourceProfile profile = new DataSourceProfile(
                dialog.getServerName(),
                dialog.getGeometry(),
                dialog.getTags(),
                dialog.getName(),
                true,
                dialog.getTagsToInclude(),
                dialog.getTagsToExclude()
            );
            dataSourceConfigModel.addProfile(profile);
        }
    }

    private void editProfileAction() {
        int index = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (index == -1) {
            return;
        }
        
        String serverName = (String) profilesTableModel.getValueAt(
                index, profilesTableModel.findColumn(COL_SERVER));
        String profileName = (String) profilesTableModel.getValueAt(
                index, profilesTableModel.findColumn(COL_PROFILE));
        DataSourceProfile profile = dataSourceConfigModel.getProfileByName(serverName, profileName);
        
        ProfileEditDialog dialog = new ProfileEditDialog(dataSourceConfigModel.getServers(), profile);
        if (dialog.showGui().getValue() == 1) {
            DataSourceProfile newProfile = new DataSourceProfile(
                dialog.getServerName(),
                dialog.getGeometry(),
                dialog.getTags(),
                dialog.getName(),
                profile.isVisible(),
                dialog.getTagsToInclude(),
                dialog.getTagsToExclude()
            );
            dataSourceConfigModel.updateProfile(profile, newProfile);
        }
    }

    private void removeProfileAction() {
        int index = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (index == -1) {
            return;
        }
        
        String serverName = (String) profilesTableModel.getValueAt(
                index, profilesTableModel.findColumn(COL_SERVER));
        String profileName = (String) profilesTableModel.getValueAt(
                index, profilesTableModel.findColumn(COL_PROFILE));
        DataSourceProfile profile = dataSourceConfigModel.getProfileByName(serverName, profileName);
        
        if (profile != null) {
            dataSourceConfigModel.removeProfile(profile);
        }
    }

    private void bulkEditAction() {
        String serversJson = org.openstreetmap.josm.plugins.mdbuildings.DataSourceServer.toJson(
                dataSourceConfigModel.getServers()).toString();
        String profilesJson = org.openstreetmap.josm.plugins.mdbuildings.DataSourceProfile.toJson(
                dataSourceConfigModel.getProfiles()).toString();
        
        BulkEditDialog dialog = new BulkEditDialog(serversJson, profilesJson);
        
        if (dialog.showGui().getValue() == 1) {
            try {
                dataSourceConfigModel.bulkUpdate(dialog.getServersJson(), dialog.getProfilesJson());
            } catch (Exception e) {
                Logging.error(e);
                JLabel label = new JLabel(tr("Error parsing JSON config. Please check the format."));
                JOptionPane.showMessageDialog(null, label, tr("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String getTabTitle() {
        return tr("Data sources");
    }

    @Override
    public Component getTabView() {
        return settingsDataSourcesPanelView;
    }

    private final class ImportOneDsStrategyComboBoxItemChanged implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                int selectedIndex = settingsDataSourcesPanelView.getImportOneDsStrategyComboBoxSelectedIndex();
                CombineNearestOneDsStrategy strategy =
                    (CombineNearestOneDsStrategy) importOneDsStrategyComboBoxModel.getElementAt(selectedIndex);
                BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(strategy.toString());
            }
        }
    }

}
