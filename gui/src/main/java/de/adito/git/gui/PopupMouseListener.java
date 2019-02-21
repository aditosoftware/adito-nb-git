package de.adito.git.gui;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author m.kaspera 07.11.2018
 */
public class PopupMouseListener extends MouseAdapter
{

  private final JPopupMenu popupMenu;
  private Action doubleClickAction;

  public PopupMouseListener(JPopupMenu pPopupMenu)
  {
    popupMenu = pPopupMenu;
  }

  /**
   * sets the Action that is executed when a doubleClick event is noticed
   *
   * @param pDoubleClickAction Action
   */
  public void setDoubleClickAction(Action pDoubleClickAction)
  {
    doubleClickAction = pDoubleClickAction;
  }

  @Override
  public void mousePressed(MouseEvent pEvent)
  {
    if (pEvent.getClickCount() == 2 && doubleClickAction != null)
    {
      doubleClickAction.actionPerformed(null);
    }
  }

  @Override
  public void mouseReleased(MouseEvent pE)
  {
    if (pE.isPopupTrigger())
    {
      if (pE.getSource() instanceof JTable)
      {
        JTable source = (JTable) pE.getSource();
        int row = source.rowAtPoint(pE.getPoint());
        int column = source.columnAtPoint(pE.getPoint());

        // if the row the user right-clicked on is not selected -> set it selected
        if (!source.isRowSelected(row))
          source.changeSelection(row, column, false, false);

      }
      else if (pE.getSource() instanceof JTree)
      {
        JTree source = (JTree) pE.getSource();
        TreePath sourcePath = source.getClosestPathForLocation(pE.getX(), pE.getY());
        if (!source.isPathSelected(sourcePath))
          source.getSelectionModel().setSelectionPath(sourcePath);
      }
      if (popupMenu != null)
      {
        for (Component component : popupMenu.getComponents())
          component.setEnabled(component.isEnabled());

        popupMenu.show((JComponent) pE.getSource(), pE.getX(), pE.getY());
      }

    }
  }
}
