package de.adito.git.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author m.kaspera 07.11.2018
 */
public class PopupMouseListener extends MouseAdapter
{

  private final JPopupMenu popupMenu;

  public PopupMouseListener(JPopupMenu pPopupMenu)
  {
    popupMenu = pPopupMenu;
  }

  @Override
  public void mouseReleased(MouseEvent pE)
  {
    if (SwingUtilities.isRightMouseButton(pE))
    {
      JTable source = (JTable) pE.getSource();
      int row = source.rowAtPoint(pE.getPoint());
      int column = source.columnAtPoint(pE.getPoint());

      // if the row the user right-clicked on is not selected -> set it selected
      if (!source.isRowSelected(row))
        source.changeSelection(row, column, false, false);

      if (popupMenu != null)
      {
        for (Component component : popupMenu.getComponents())
          component.setEnabled(component.isEnabled());

        popupMenu.show(source, pE.getX(), pE.getY());
      }

    }
  }
}
