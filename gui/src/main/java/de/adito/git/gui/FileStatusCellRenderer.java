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
        if (!isSelected) {
            label.setForeground(((EChangeType) table.getModel().getValueAt(row, 2)).getStatusColor());
        }
        return label;
    }
}
