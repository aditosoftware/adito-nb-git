package de.adito.git.gui.popup;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiFunction;

/**
 * An abstract class to handle all mouse handler
 *
 * @author a.arnold, 15.11.2018
 */
class MouseDragHandler extends MouseAdapter
{

  private static final BiFunction<Rectangle, Point, Rectangle> northResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x, pRectangle.y + pPoint.y, pRectangle.width, pRectangle.height - pPoint.y));
  private static final BiFunction<Rectangle, Point, Rectangle> northEastResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x, pRectangle.y + pPoint.y, pRectangle.width + pPoint.x, pRectangle.height - pPoint.y));
  private static final BiFunction<Rectangle, Point, Rectangle> northWestResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x + pPoint.x, pRectangle.y + pPoint.y, pRectangle.width - pPoint.x, pRectangle.height - pPoint.y));
  private static final BiFunction<Rectangle, Point, Rectangle> southResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x, pRectangle.y, pRectangle.width, pRectangle.height + pPoint.y));
  private static final BiFunction<Rectangle, Point, Rectangle> southEastResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x, pRectangle.y, pRectangle.width + pPoint.x, pRectangle.height + pPoint.y));
  private static final BiFunction<Rectangle, Point, Rectangle> southWestResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x + pPoint.x, pRectangle.y, pRectangle.width - pPoint.x, pRectangle.height + pPoint.y));
  private static final BiFunction<Rectangle, Point, Rectangle> eastResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x, pRectangle.y, pRectangle.width + pPoint.x, pRectangle.height));
  private static final BiFunction<Rectangle, Point, Rectangle> westResizeFunction = ((pRectangle, pPoint)
      -> new Rectangle(pRectangle.x + pPoint.x, pRectangle.y, pRectangle.width - pPoint.x, pRectangle.height));

  private final PopupWindow window;
  private final int cursorType;
  private final Point mouseBefore;
  private Rectangle rectangle;
  private Dimension minDimension;

  private final BiFunction<Rectangle, Point, Rectangle> resizeFunction;

  public MouseDragHandler(PopupWindow pWindow, int pCursorType)
  {
    window = pWindow;
    minDimension = new Dimension(125, 200);
    cursorType = pCursorType;
    mouseBefore = new Point();
    rectangle = new Rectangle(new Point(), new Dimension());
    switch (pCursorType)
    {
      case Cursor.N_RESIZE_CURSOR:
        resizeFunction = northResizeFunction;
        break;
      case Cursor.NE_RESIZE_CURSOR:
        resizeFunction = northEastResizeFunction;
        break;
      case Cursor.NW_RESIZE_CURSOR:
        resizeFunction = northWestResizeFunction;
        break;
      case Cursor.S_RESIZE_CURSOR:
        resizeFunction = southResizeFunction;
        break;
      case Cursor.SE_RESIZE_CURSOR:
        resizeFunction = southEastResizeFunction;
        break;
      case Cursor.SW_RESIZE_CURSOR:
        resizeFunction = southWestResizeFunction;
        break;
      case Cursor.W_RESIZE_CURSOR:
        resizeFunction = westResizeFunction;
        break;
      case Cursor.E_RESIZE_CURSOR:
        resizeFunction = eastResizeFunction;
        break;
      default:
        resizeFunction = ((pRectangle, pPoint) -> pRectangle);
    }
  }

  Rectangle getRectangle()
  {
    return rectangle;
  }

  Point getDistance(MouseEvent e)
  {
    Point p = new Point();
    p.x = e.getLocationOnScreen().x - mouseBefore.x;
    p.y = e.getLocationOnScreen().y - mouseBefore.y;
    return p;
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    mouseBefore.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y);
    rectangle = new Rectangle(window.getLocation(), window.getSize());
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    window.setCursor(Cursor.getPredefinedCursor(cursorType));
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    window.setCursor(null);
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    Rectangle calculatedRec = calc(e);
    if (calculatedRec.getWidth() < minDimension.width)
    {
      calculatedRec.setSize(minDimension.width, calculatedRec.height);
      calculatedRec.setLocation(window.getX(), calculatedRec.y);
    }
    if (calculatedRec.getHeight() < minDimension.height)
    {
      calculatedRec.setSize(calculatedRec.width, minDimension.height);
      calculatedRec.setLocation(calculatedRec.x, window.getY());
    }
    window.setBounds(calculatedRec);
  }

  protected Rectangle calc(MouseEvent e)
  {
    Rectangle rectangle = getRectangle();
    Point distance = getDistance(e);
    return resizeFunction.apply(rectangle, distance);
  }
}
