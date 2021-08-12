package de.adito.git.gui;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

/**
 * @author m.kaspera 07.11.2018
 */
public class PopupMouseListener extends MouseAdapter
{

  private final Supplier<JPopupMenu> popupMenuSupplier;
  private Action doubleClickAction;

  public PopupMouseListener(Supplier<JPopupMenu> pPopupMenuSupplier)
  {
    popupMenuSupplier = pPopupMenuSupplier;
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
      if (pEvent.getSource() instanceof JTree)
      {
        JTree source = (JTree) pEvent.getSource();
        TreePath sourcePath = source.getClosestPathForLocation(pEvent.getX(), pEvent.getY());
        if (source.isPathSelected(sourcePath) && source.getModel().isLeaf(sourcePath.getLastPathComponent()))
          doubleClickAction.actionPerformed(null);
      }
      else
      {
        doubleClickAction.actionPerformed(null);
      }
    }
    if (pEvent.isPopupTrigger())
    {
      if (pEvent.getSource() instanceof JTable)
      {
        JTable source = (JTable) pEvent.getSource();
        int row = source.rowAtPoint(pEvent.getPoint());
        int column = source.columnAtPoint(pEvent.getPoint());

        // if the row the user right-clicked on is not selected -> set it selected
        if (!source.isRowSelected(row))
          source.changeSelection(row, column, false, false);

      }
      else if (pEvent.getSource() instanceof JTree)
      {
        JTree source = (JTree) pEvent.getSource();
        TreePath sourcePath = source.getClosestPathForLocation(pEvent.getX(), pEvent.getY());
        if (!source.isPathSelected(sourcePath))
          source.getSelectionModel().setSelectionPath(sourcePath);
      }
      JPopupMenu popupMenu = popupMenuSupplier.get();
      if (popupMenu != null)
      {
        for (Component component : popupMenu.getComponents())
          component.setEnabled(component.isEnabled());

        popupMenu.show((JComponent) pEvent.getSource(), pEvent.getX(), pEvent.getY());
      }

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
      JPopupMenu popupMenu = popupMenuSupplier.get();
      if (popupMenu != null)
      {
        for (Component component : popupMenu.getComponents())
          component.setEnabled(component.isEnabled());

        popupMenu.show((JComponent) pE.getSource(), pE.getX(), pE.getY());
      }

    }
  }
}
