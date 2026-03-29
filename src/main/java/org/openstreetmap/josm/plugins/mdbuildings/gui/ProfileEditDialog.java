package org.openstreetmap.josm.plugins.mdbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.openstreetmap.josm.plugins.mdbuildings.WfsMetadataFetcher;
import org.openstreetmap.josm.plugins.mdbuildings.DataSourceProfile;
import org.openstreetmap.josm.plugins.mdbuildings.DataSourceServer;

public class ProfileEditDialog {
    private final JTextField nameField;
    private final JComboBox<String> serverComboBox;
    private final JTextField tagsField;
    private final JTextField geometryField;
    private final JTextField tagsToIncludeField;
    private final JTextField tagsToExcludeField;
    private final List<DataSourceServer> servers;
    private int result;

    public ProfileEditDialog(List<DataSourceServer> servers, DataSourceProfile profile) {
        this.servers = servers;
        nameField = new JTextField(profile != null ? profile.getName() : "", 20);
        serverComboBox = new JComboBox<>(servers.stream().map(DataSourceServer::getName).toArray(String[]::new));
        if (profile != null) {
            serverComboBox.setSelectedItem(profile.getDataSourceServerName());
        }
        tagsField = new JTextField(profile != null ? profile.getTags() : "", 20);
        geometryField = new JTextField(profile != null ? profile.getGeometry() : "", 20);
        tagsToIncludeField = new JTextField(profile != null ? profile.getTagsToInclude() : "", 20);
        tagsToIncludeField.setToolTipText(tr("Comma separated list of tags to INCLUDE (whitelist)."
                + " If empty, all tags are included."));
        tagsToExcludeField = new JTextField(profile != null ? profile.getTagsToExclude() : "", 20);
        tagsToExcludeField.setToolTipText(tr("Comma separated list of tags to EXCLUDE (blacklist)."));
    }

    public ProfileEditDialog showGui() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel panel = new JPanel(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel(tr("Profile Name:")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(nameField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel(tr("Server:")), gbc);
        gbc.gridx = 1;
        panel.add(serverComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel(tr("Tags Source:")), gbc);
        gbc.gridx = 1;
        panel.add(tagsField, gbc);
        JButton fetchLayersBtn = new JButton(tr("Fetch Layers"));
        gbc.gridx = 2;
        panel.add(fetchLayersBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel(tr("Geometry Source:")), gbc);
        gbc.gridx = 1;
        panel.add(geometryField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel(tr("Include Tags:")), gbc);
        gbc.gridx = 1;
        panel.add(tagsToIncludeField, gbc);
        JButton selectTagsBtn = new JButton(tr("Select Tags"));
        gbc.gridx = 2;
        panel.add(selectTagsBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel(tr("Exclude Tags:")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(tagsToExcludeField, gbc);
        gbc.gridwidth = 1;

        fetchLayersBtn.addActionListener(e -> {
            String serverName = (String) serverComboBox.getSelectedItem();
            DataSourceServer server = servers.stream()
                .filter(s -> s.getName().equals(serverName))
                .findFirst().orElse(null);
            if (server != null) {
                try {
                    List<String> layers = WfsMetadataFetcher.fetchLayers(server.getUrl());
                    Object[] layersArr = layers.toArray();
                    String selected = (String) JOptionPane.showInputDialog(
                        panel, tr("Select Layer"), tr("Layers"),
                        JOptionPane.PLAIN_MESSAGE, null, layersArr, geometryField.getText()
                    );
                    if (selected != null) {
                        geometryField.setText(selected);
                        tagsField.setText(selected);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        panel, tr("Error fetching layers: {0}", ex.getMessage()),
                        tr("Error"), JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        selectTagsBtn.addActionListener(e -> {
            String serverName = (String) serverComboBox.getSelectedItem();
            DataSourceServer server = servers.stream()
                .filter(s -> s.getName().equals(serverName))
                .findFirst().orElse(null);
            String layer = geometryField.getText();
            if (server != null && !layer.isEmpty()) {
                try {
                    List<String> attributes = WfsMetadataFetcher.fetchAttributes(server.getUrl(), layer);
                    showTagSelectionDialog(panel, attributes);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        panel, tr("Error fetching attributes: {0}", ex.getMessage()),
                        tr("Error"), JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                    panel, tr("Please select a server and layer first."),
                    tr("Info"), JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        result = JOptionPane.showConfirmDialog(
            null, panel, tr("Edit Profile"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        return this;
    }

    private void showTagSelectionDialog(JPanel parent, List<String> attributes) {
        JPanel panel = new JPanel(new java.awt.GridLayout(0, 3));
        java.util.Set<String> currentSelected = java.util.Arrays.stream(tagsToIncludeField.getText().split(","))
            .map(String::trim).collect(java.util.stream.Collectors.toSet());
        
        java.util.List<JCheckBox> checkBoxes = new java.util.ArrayList<>();
        for (String attr : attributes) {
            boolean isIncluded = currentSelected.contains(attr) || tagsToIncludeField.getText().isEmpty();
            JCheckBox cb = new JCheckBox(attr, isIncluded);
            checkBoxes.add(cb);
            panel.add(cb);
        }

        int res = JOptionPane.showConfirmDialog(
            parent, new JScrollPane(panel),
            tr("Select Tags to Include"), JOptionPane.OK_CANCEL_OPTION
        );
        if (res == JOptionPane.OK_OPTION) {
            String selected = checkBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(JCheckBox::getText)
                .collect(java.util.stream.Collectors.joining(","));
            tagsToIncludeField.setText(selected);
        }
    }

    public int getValue() {
        return result == JOptionPane.OK_OPTION ? 1 : 0;
    }

    public String getName() {
        return nameField.getText();
    }

    public String getServerName() {
        return (String) serverComboBox.getSelectedItem();
    }

    public String getTags() {
        return tagsField.getText();
    }

    public String getGeometry() {
        return geometryField.getText();
    }

    public String getTagsToInclude() {
        return tagsToIncludeField.getText();
    }

    public String getTagsToExclude() {
        return tagsToExcludeField.getText();
    }
}
