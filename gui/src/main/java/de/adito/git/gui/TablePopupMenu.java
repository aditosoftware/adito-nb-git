package de.adito.git.gui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * creates popup menus for tables, based on the list of actions passed
 *
 * @author m.kaspera 04.10.2018
 */
public class TablePopupMenu extends JPopupMenu {

    private JTable table;
    private List<AbstractTableAction> tableActions;
    private _PopupMouseListener mouseListener;

    public TablePopupMenu(JTable pTable, List<AbstractTableAction> pTableActions){
        table = pTable;
        tableActions = pTableActions;
        for(AbstractTableAction action: tableActions){
            add(action);
        }
        mouseListener = new _PopupMouseListener();
    }

    /**
     * this method has to be called for the popup menu to appear on rightclick
     */
    public void activateMouseListener(){
        table.addMouseListener(mouseListener);
    }

    /**
     * removes the listener, popup menu will no longer appear on rightclick
     */
    public void removeMouseListenter(){
        table.removeMouseListener(mouseListener);
    }

    /**
     * Listener that displays the popup menu on right-click and notifies the actions which rows are selected
     */
    private class _PopupMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                JTable source = (JTable) e.getSource();
                int row = source.rowAtPoint(e.getPoint());
                int column = source.columnAtPoint(e.getPoint());

                // if the row the user right-clicked on is not selected -> set it selected
                if (!source.isRowSelected(row))
                    source.changeSelection(row, column, false, false);
                // get selected rows and notify the Actions which rows are selected
                int[] selectedRows = table.getSelectedRows();
                for(AbstractTableAction tableAction: tableActions){
                    tableAction.setRows(selectedRows);
                }
                show(table, e.getX(), e.getY());
            }
        }
    }
}
