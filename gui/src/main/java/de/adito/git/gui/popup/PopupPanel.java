package de.adito.git.gui.popup;

import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;

/**
 * A Panel for all mouse handlers.
 * This {@link JPanel} is a "popup" without border.
 *
 * @author a.arnold, 22.11.2018
 */
class PopupPanel extends JPanel
{
  protected static final int DRAG_BORDER_WIDTH = 5;
  private static final String SECONDARY_BACKGROUND_COLOR_KEY = "adito.secondary.background.color";

  PopupPanel(JComponent pComponent, String pTitle, PopupWindow pWindow)
  {
    setBorder(new LineBorder(Color.gray));
    final int gap = 5;
    double fill = TableLayout.FILL;
    double[] cols = {DRAG_BORDER_WIDTH, fill, DRAG_BORDER_WIDTH};
    double[] rows = {DRAG_BORDER_WIDTH,
                     TableLayout.PREFERRED, //Title
                     gap,
                     fill, //Content
                     DRAG_BORDER_WIDTH};

    setLayout(new TableLayout(cols, rows));
    TitlePanel titlePanel = new TitlePanel(new HandlerMovement(pWindow), pTitle);
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(0, 0, new MouseSensor(new MouseDragHandler(pWindow, Cursor.NW_RESIZE_CURSOR), UIManager.getColor(SECONDARY_BACKGROUND_COLOR_KEY)));
    tlu.add(1, 0, new MouseSensor(new MouseDragHandler(pWindow, Cursor.N_RESIZE_CURSOR), UIManager.getColor(SECONDARY_BACKGROUND_COLOR_KEY)));
    tlu.add(2, 0, new MouseSensor(new MouseDragHandler(pWindow, Cursor.NE_RESIZE_CURSOR), UIManager.getColor(SECONDARY_BACKGROUND_COLOR_KEY)));
    tlu.add(0, 1, 0, 3, new EastWestColoredMouseDragHandler(pWindow, titlePanel, Cursor.W_RESIZE_CURSOR));
    tlu.add(1, 1, titlePanel);
    tlu.add(2, 1, 2, 3, new EastWestColoredMouseDragHandler(pWindow, titlePanel, Cursor.E_RESIZE_CURSOR));
    tlu.add(1, 2, new MouseSensor(new HandlerMovement(pWindow)));
    tlu.add(1, 3, _createContentPanel(pComponent));
    tlu.add(0, 4, new MouseSensor(new MouseDragHandler(pWindow, Cursor.SW_RESIZE_CURSOR)));
    tlu.add(1, 4, new MouseSensor(new MouseDragHandler(pWindow, Cursor.S_RESIZE_CURSOR)));
    tlu.add(2, 4, new MouseSensor(new MouseDragHandler(pWindow, Cursor.SE_RESIZE_CURSOR)));
  }

  private JComponent _createContentPanel(JComponent pComponent)
  {
    JScrollPane contentScrollPane = new JScrollPane(pComponent);
    contentScrollPane.getVerticalScrollBar().setUnitIncrement(16);

    contentScrollPane.setViewportBorder(null);
    contentScrollPane.getViewport().setBorder(null);
    contentScrollPane.setBorder(null);
    return contentScrollPane;
  }

  /**
   * MouseDragHandler that has the secondary background color for the upper height of the titlePanel
   */
  private static class EastWestColoredMouseDragHandler extends MouseSensor
  {
    private final TitlePanel titlePanel;

    public EastWestColoredMouseDragHandler(PopupWindow pWindow, TitlePanel pTitlePanel, int pCursorType)
    {
      super(new MouseDragHandler(pWindow, pCursorType));
      titlePanel = pTitlePanel;
    }

    @Override
    public void paint(Graphics g)
    {
      super.paint(g);
      g.setColor(UIManager.getColor(SECONDARY_BACKGROUND_COLOR_KEY));
      g.fillRect(0, 0, g.getClipBounds().width, titlePanel.getHeight());
    }
  }
}