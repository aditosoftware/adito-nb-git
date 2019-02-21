package de.adito.git.gui;

import de.adito.git.api.data.EChangeType;
import de.adito.git.gui.tablemodels.StatusTableModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

/**
 * Colors the labels in the Status list and the list of files to commit according to
 * the EChangeType they have
 *
 * @author m.kaspera 05.10.2018
 */
public class FileStatusCellRenderer extends DefaultTableCellRenderer
{

  @Override
  public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected, boolean pHasFocus, int pRow, int pColumn)
  {
    // Text in cells is always displayed as label
    JLabel label = (JLabel) super.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);
    if (!pIsSelected)
    {
      label.setForeground(((EChangeType) pTable.getModel().getValueAt(pRow, ((AbstractTableModel) pTable.getModel())
          .findColumn(StatusTableModel.CHANGE_TYPE_COLUMN_NAME))).getStatusColor());
    }
    return label;
  }
}
