package de.adito.git.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author m.kaspera 07.11.2018
 */
public class PopupMouseListener extends MouseAdapter {

    private final JPopupMenu popupMenu;

    public PopupMouseListener(JPopupMenu pPopupMenu) {
        popupMenu = pPopupMenu;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            JTable source = (JTable) e.getSource();
            int row = source.rowAtPoint(e.getPoint());
            int column = source.columnAtPoint(e.getPoint());

            // if the row the user right-clicked on is not selected -> set it selected
            if (!source.isRowSelected(row))
                source.changeSelection(row, column, false, false);

            if (popupMenu != null) {
                for (Component component : popupMenu.getComponents())
                    component.setEnabled(component.isEnabled());

                popupMenu.show(source, e.getX(), e.getY());
            }

        }
    }
}
