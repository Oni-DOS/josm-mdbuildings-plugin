package org.openstreetmap.josm.plugins.mdbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class BulkEditDialog {
    private final JTextArea serversArea;
    private final JTextArea profilesArea;
    private int result;

    public BulkEditDialog(String serversJson, String profilesJson) {
        serversArea = new JTextArea(serversJson);
        profilesArea = new JTextArea(profilesJson);
    }

    public BulkEditDialog showGui() {
        JPanel serversPanel = new JPanel(new BorderLayout());
        serversPanel.add(new JLabel(tr("Servers JSON:")), BorderLayout.NORTH);
        serversPanel.add(new JScrollPane(serversArea), BorderLayout.CENTER);
        
        JPanel profilesPanel = new JPanel(new BorderLayout());
        profilesPanel.add(new JLabel(tr("Profiles JSON:")), BorderLayout.NORTH);
        profilesPanel.add(new JScrollPane(profilesArea), BorderLayout.CENTER);
        
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.add(serversPanel);
        mainPanel.add(profilesPanel);
        mainPanel.setPreferredSize(new Dimension(600, 500));

        result = JOptionPane.showConfirmDialog(
            null, mainPanel, tr("Bulk Edit Config (JSON)"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        return this;
    }

    public int getValue() {
        return result == JOptionPane.OK_OPTION ? 1 : 0;
    }

    public String getServersJson() {
        return serversArea.getText();
    }

    public String getProfilesJson() {
        return profilesArea.getText();
    }
}
