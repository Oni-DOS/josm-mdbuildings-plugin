package org.openstreetmap.josm.plugins.mdbuildings.gui;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SettingsStatsPanel extends JPanel {

    public SettingsStatsPanel() {
        super(new GridBagLayout());
        setBorder(new EmptyBorder(4, 6, 4, 6));
    }

    public void updateStats(Map<String, String> stats) {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        for (Map.Entry<String, String> entry : stats.entrySet()) {
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.weightx = 0.3;
            add(new JLabel(entry.getKey() + ":"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            add(new JLabel(entry.getValue()), gbc);
            row++;
        }
        
        // Push everything to the top
        gbc.gridy = row;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);
        
        revalidate();
        repaint();
    }
}
