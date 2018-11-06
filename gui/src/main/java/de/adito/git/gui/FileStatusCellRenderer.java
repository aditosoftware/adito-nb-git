package de.adito.git.gui;

import de.adito.git.api.data.EChangeType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Colors the labels in the Status list and the list of files to commit according to
 * the EChangeType they have
 *
 * @author m.kaspera 05.10.2018
 */
public class FileStatusCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Text in cells is always displayed as label
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (table.getModel().getValueAt(row, 2).equals(EChangeType.ADD)) {
            // Foreground  = text color
            label.setForeground(new Color(106, 135, 89));
        } else if (table.getModel().getValueAt(row, 2).equals(EChangeType.NEW)) {
            label.setForeground(new Color(106, 135, 89));
        } else if (table.getModel().getValueAt(row, 2).equals(EChangeType.MODIFY)) {
            label.setForeground(new Color(77, 130, 184));
        } else if (table.getModel().getValueAt(row, 2).equals(EChangeType.CHANGED)) {
            label.setForeground(new Color(77, 130, 184));
        } else if (table.getModel().getValueAt(row, 2).equals(EChangeType.DELETE)) {
            label.setForeground(new Color(204, 120, 50));
        } else if (table.getModel().getValueAt(row, 2).equals(EChangeType.MISSING)) {
            label.setForeground(new Color(204, 120, 50));
        } else if (table.getModel().getValueAt(row, 2).equals(EChangeType.CONFLICTING)) {
            label.setForeground(new Color(230, 132, 151));
        } else {
            label.setForeground(Color.BLACK);
        }
        return label;
    }
}
