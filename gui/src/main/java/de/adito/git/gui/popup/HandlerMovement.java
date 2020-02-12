package de.adito.git.gui.popup;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * This handler moves the popup for clicking on the middle of the window
 *
 * @author a.arnold, 15.11.2018
 */
class HandlerMovement extends MouseDragHandler
{
  HandlerMovement(PopupWindow pWindow)
  {
    super(pWindow, Cursor.getDefaultCursor().getType());
  }

  @Override
  protected Rectangle calc(MouseEvent e)
  {
    Rectangle rectangle = getCurrentArea();
    Point distance = getDragDistance(e);
    return new Rectangle(rectangle.x + distance.x, rectangle.y + distance.y, rectangle.width, rectangle.height);
  }
}
