package org.openstreetmap.josm.plugins.mdbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class SettingsDataSourcesPanel extends JPanel {

    private static final String SERVERS = tr("Servers");
    private static final String PROFILES = tr("Profiles");
    private static final String IMPORT_STRATEGY = tr("Import strategy");
    private static final String ADD_SERVER_TITLE = tr("Add new server");
    private static final String REMOVE_SERVER_TITLE = tr("Remove server");

    private JButton upBtn;
    private JButton downBtn;
    private JButton addServerBtn;
    private JButton editServerBtn;
    private JButton removeServerBtn;
    private JButton addProfileBtn;
    private JButton editProfileBtn;
    private JButton removeProfileBtn;
    private JButton bulkEditBtn;
    private JButton onlineHelpBtn;
    private JTextField addServerNameField;
    private JTextField addServerUrlField;

    private JList<Object> serverList;
    private JTable profileTable;
    private JComboBox<Object> importOneDsStrategyComboBox;
    private javax.swing.JCheckBox useDtmElevationCheckBox;
    private javax.swing.JCheckBox mergeInteriorAddressNodesCheckBox;
    private JTextField defaultSourceField;


    public SettingsDataSourcesPanel() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(4, 4, 4, 4));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.weightx = 1.0;

        JPanel rootPanel = new JPanel(new GridBagLayout());
        gbc.gridy = 0;
        gbc.weighty = 0.3;
        rootPanel.add(createServerListPanel(), gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.4;
        rootPanel.add(createProfileTablePanel(), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.0;
        rootPanel.add(createImportStrategyPanel(), gbc);

        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        onlineHelpBtn = new JButton(tr("Online Documentation"));
        onlineHelpBtn.setToolTipText(tr("Open the online help wiki in your browser"));
        rootPanel.add(onlineHelpBtn, gbc);

        gbc.anchor = GridBagConstraints.EAST;
        bulkEditBtn = new JButton(tr("Bulk Edit (JSON)"));
        rootPanel.add(bulkEditBtn, gbc);

        gbc.gridy = 4;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(new JPanel(), gbc);

        add(new JScrollPane(rootPanel), BorderLayout.CENTER);
    }

    private Component createServerListPanel() {
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createTitledBorder(SERVERS));

        this.serverList = new JList<>();
        this.serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.serverList.setVisibleRowCount(5);

        final JScrollPane jScrollPane = new JScrollPane(serverList);

        final JPanel buttonsPanel = new JPanel();
        addServerBtn = new JButton(tr("Add"));
        editServerBtn = new JButton(tr("Edit"));
        removeServerBtn = new JButton(tr("Remove"));
        editServerBtn.setEnabled(false);
        removeServerBtn.setEnabled(false);
        buttonsPanel.add(addServerBtn);
        buttonsPanel.add(editServerBtn);
        buttonsPanel.add(removeServerBtn);

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }

    public JPanel createServerConfirmDialog() {
        final JPanel dialogPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(groupLayout);

        addServerNameField = new JTextField(20);
        addServerUrlField = new JTextField(20);

        JLabel serverNameLabel = new JLabel(tr("Server name:"));
        JLabel serverUrlLabel = new JLabel(tr("Server URL:") + " ");

        serverNameLabel.setLabelFor(addServerNameField);
        serverUrlLabel.setLabelFor(addServerUrlField);

        GroupLayout.SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
        horizontalGroup.addGroup(
            groupLayout
                .createParallelGroup()
                .addComponent(serverNameLabel)
                .addComponent(serverUrlLabel)
        );
        horizontalGroup.addGroup(
            groupLayout
                .createParallelGroup()
                .addComponent(addServerNameField)
                .addComponent(addServerUrlField)
        );
        groupLayout.setHorizontalGroup(horizontalGroup);

        GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
        verticalGroup.addGroup(
            groupLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(serverNameLabel)
                .addComponent(addServerNameField)
        );
        verticalGroup.addGroup(
            groupLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(serverUrlLabel)
                .addComponent(addServerUrlField)
        );
        groupLayout.setVerticalGroup(verticalGroup);
        return dialogPanel;
    }

    private JPanel createImportStrategyPanel() {
        JPanel strategyPanel = new JPanel(new BorderLayout());
        strategyPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(IMPORT_STRATEGY)
        ));

        importOneDsStrategyComboBox = new JComboBox<>();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 5);
        c.weightx = 0;
        JPanel comboBoxesPanel = new JPanel(new GridBagLayout());
        comboBoxesPanel.add(new JLabel(tr("On data source missing:")), c);
        
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        comboBoxesPanel.add(importOneDsStrategyComboBox, c);

        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(5, 0, 0, 0);
        useDtmElevationCheckBox = new javax.swing.JCheckBox(
                tr("Use DTM source for building height calculations"));
        comboBoxesPanel.add(useDtmElevationCheckBox, c);

        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(5, 0, 0, 0);
        mergeInteriorAddressNodesCheckBox = new javax.swing.JCheckBox(
                tr("Merge interior address nodes into building"));
        comboBoxesPanel.add(mergeInteriorAddressNodesCheckBox, c);

        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 5);
        comboBoxesPanel.add(new JLabel(tr("Default source tag:")), c);

        c.weightx = 1.0;
        c.insets = new Insets(5, 0, 0, 0);
        defaultSourceField = new JTextField();
        comboBoxesPanel.add(defaultSourceField, c);
        
        comboBoxesPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        strategyPanel.add(comboBoxesPanel, BorderLayout.CENTER);

        return strategyPanel;
    }

    public boolean promptNewServerNameUrl() {
        int result = JOptionPane.showConfirmDialog(
            null,
            createServerConfirmDialog(),
            ADD_SERVER_TITLE,
            JOptionPane.OK_CANCEL_OPTION
        );
        return result == JOptionPane.OK_OPTION;
    }

    public void showAddNewServerErrorDialog() {
        JOptionPane.showMessageDialog(
            null,
            tr("Error adding new server. The name must be unique, and the URL must be valid!"),
            ADD_SERVER_TITLE,
            JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean showRemoveServerConfirmDialog(String serverName) {
        int result = JOptionPane.showConfirmDialog(
            null,
            tr("Are you sure to remove server {0}?", serverName),
            REMOVE_SERVER_TITLE,
            JOptionPane.OK_CANCEL_OPTION
        );
        return result == JOptionPane.OK_OPTION;
    }

    private Component createProfileTablePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBorder(BorderFactory.createTitledBorder(PROFILES));

        this.profileTable = new JTable();
        this.profileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JToolBar moveToolBar = new JToolBar(SwingConstants.VERTICAL);
        moveToolBar.setFloatable(false);

        upBtn = new JButton(org.openstreetmap.josm.tools.ImageProvider.getIfAvailable("dialogs/up.svg"));
        if (upBtn.getIcon() == null) {
            upBtn.setIcon(org.openstreetmap.josm.tools.ImageProvider.getIfAvailable("up"));
        }

        downBtn = new JButton(org.openstreetmap.josm.tools.ImageProvider.getIfAvailable("dialogs/down.svg"));
        if (downBtn.getIcon() == null) {
            downBtn.setIcon(org.openstreetmap.josm.tools.ImageProvider.getIfAvailable("down"));
        }
        
        upBtn.setEnabled(false);
        downBtn.setEnabled(false);

        moveToolBar.add(upBtn);
        moveToolBar.add(downBtn);
        
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.add(moveToolBar, BorderLayout.NORTH);

        addProfileBtn = new JButton(tr("Add"));
        editProfileBtn = new JButton(tr("Edit"));
        removeProfileBtn = new JButton(tr("Remove"));
        editProfileBtn.setEnabled(false);
        removeProfileBtn.setEnabled(false);
        
        JPanel crudPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        crudPanel.add(addProfileBtn);
        crudPanel.add(editProfileBtn);
        crudPanel.add(removeProfileBtn);
        
        actionPanel.add(crudPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(profileTable);
        profilePanel.add(scrollPane, BorderLayout.CENTER);
        profilePanel.add(actionPanel, BorderLayout.EAST);

        return profilePanel;
    }

    public void upBtnAddActionListener(ActionListener listener) {
        upBtn.addActionListener(listener);
    }

    public void downBtnAddActionListener(ActionListener listener) {
        downBtn.addActionListener(listener);
    }


    public void addImportOneDsStrategyComboBoxItemListener(ItemListener listener) {
        importOneDsStrategyComboBox.addItemListener(listener);
    }

    public void addMergeInteriorAddressNodesCheckBoxItemListener(ItemListener listener) {
        mergeInteriorAddressNodesCheckBox.addItemListener(listener);
    }

    public void upBtnSetEnabled(Boolean enabled) {
        upBtn.setEnabled(enabled);
    }

    public void downBtnSetEnabled(Boolean enabled) {
        downBtn.setEnabled(enabled);
    }

    public void removeServerBtnSetEnabled(Boolean enabled) {
        removeServerBtn.setEnabled(enabled);
    }

    public void profilesTableAddListSelectionListener(ListSelectionListener listener) {
        profileTable.getSelectionModel().addListSelectionListener(listener);
    }

    public void profilesTableClearSelection() {
        profileTable.getSelectionModel().clearSelection();
        profileTable.getColumnModel().getSelectionModel().clearSelection();
    }

    public void setProfilesTableModel(TableModel model) {
        this.profileTable.setModel(model);
        this.profileTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Resize when model is set
        javax.swing.SwingUtilities.invokeLater(this::autoResizeColumnWidths);
        
        // And whenever data changes
        model.addTableModelListener(e -> {
            javax.swing.SwingUtilities.invokeLater(this::autoResizeColumnWidths);
        });
    }

    /**
     * Resize each column to the widest text (header or any cell value), measured with FontMetrics.
     */
    private void autoResizeColumnWidths() {
        java.awt.FontMetrics fm = profileTable.getFontMetrics(profileTable.getFont());
        java.awt.FontMetrics headerFm = profileTable.getTableHeader().getFontMetrics(
            profileTable.getTableHeader().getFont());
        final int padding = 4;

        TableColumnModel colModel = profileTable.getColumnModel();
        for (int col = 0; col < colModel.getColumnCount(); col++) {
            TableColumn tableColumn = colModel.getColumn(col);

            // Measure header text
            String headerVal = tableColumn.getHeaderValue() == null
                ? "" : tableColumn.getHeaderValue().toString();
            int maxWidth = headerFm.stringWidth(headerVal) + padding;

            // Measure each row's cell text
            for (int row = 0; row < profileTable.getRowCount(); row++) {
                Object val = profileTable.getValueAt(row, col);
                if (val != null) {
                    maxWidth = Math.max(maxWidth, fm.stringWidth(val.toString()) + padding);
                }
            }

            tableColumn.setPreferredWidth(maxWidth);
            tableColumn.setWidth(maxWidth);
        }
    }

    public int getProfilesTableSelectedRowIndex() {
        return profileTable.getSelectedRow();
    }

    public int getImportOneDsStrategyComboBoxSelectedIndex() {
        return importOneDsStrategyComboBox.getSelectedIndex();
    }

    public int getProfilesTableRowCount() {
        return profileTable.getRowCount();
    }

    public void addServerBtnAddActionListener(ActionListener listener) {
        addServerBtn.addActionListener(listener);
    }

    public void removeServerBtnAddActionListener(ActionListener listener) {
        removeServerBtn.addActionListener(listener);
    }

    public int getServerListSelectedIndex() {
        return serverList.getSelectedIndex();
    }

    public String getAddServerNameFieldText() {
        return addServerNameField.getText();
    }

    public String getAddServerUrlFieldText() {
        return addServerUrlField.getText();
    }

    public void setServersListModel(javax.swing.ListModel<Object> serversListModel) {
        serverList.setModel(serversListModel);
    }

    public void setImportOneDsStrategyComboBoxModel(
            javax.swing.DefaultComboBoxModel<Object> importOneDsStrategyComboBoxModel) {
        importOneDsStrategyComboBox.setModel(importOneDsStrategyComboBoxModel);
    }

    public void setMergeInteriorAddressNodesCheckBoxSelected(boolean selected) {
        mergeInteriorAddressNodesCheckBox.setSelected(selected);
    }

    public void setUseDtmElevationCheckBoxSelected(boolean selected) {
        useDtmElevationCheckBox.setSelected(selected);
    }

    public void addUseDtmElevationCheckBoxItemListener(ItemListener listener) {
        useDtmElevationCheckBox.addItemListener(listener);
    }

    public void editServerBtnAddActionListener(ActionListener listener) {
        editServerBtn.addActionListener(listener);
    }

    public void addProfileBtnAddActionListener(ActionListener listener) {
        addProfileBtn.addActionListener(listener);
    }

    public void editProfileBtnAddActionListener(ActionListener listener) {
        editProfileBtn.addActionListener(listener);
    }

    public void removeProfileBtnAddActionListener(ActionListener listener) {
        removeProfileBtn.addActionListener(listener);
    }

    public void bulkEditBtnAddActionListener(ActionListener listener) {
        bulkEditBtn.addActionListener(listener);
    }

    public void onlineHelpBtnAddActionListener(ActionListener listener) {
        onlineHelpBtn.addActionListener(listener);
    }

    public void editServerBtnSetEnabled(Boolean enabled) {
        editServerBtn.setEnabled(enabled);
    }

    public void editProfileBtnSetEnabled(Boolean enabled) {
        editProfileBtn.setEnabled(enabled);
    }

    public void removeProfileBtnSetEnabled(Boolean enabled) {
        removeProfileBtn.setEnabled(enabled);
    }

    public void serversListAddListSelectionListener(ListSelectionListener listener) {
        serverList.addListSelectionListener(listener);
    }

    public String promptEditServerName(String currentName) {
        return JOptionPane.showInputDialog(null, tr("Server name:"), currentName);
    }

    public String promptEditServerUrl(String currentUrl) {
        return JOptionPane.showInputDialog(null, tr("Server URL:"), currentUrl);
    }

    public void setDefaultSourceFieldText(String text) {
        defaultSourceField.setText(text);
    }

    public String getDefaultSourceFieldText() {
        return defaultSourceField.getText();
    }

    public void addDefaultSourceFieldDocumentListener(javax.swing.event.DocumentListener listener) {
        defaultSourceField.getDocument().addDocumentListener(listener);
    }
}
