package de.adito.git.gui.popup;

import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

/**
 * A Panel for all mouse handlers.
 * This {@link JPanel} is a "popup" without border.
 *
 * @author a.arnold, 22.11.2018
 */
class PopupPanel extends JPanel
{

  PopupPanel(JComponent pComponent, String pTitle, PopupWindow pWindow)
  {
    setBorder(new LineBorder(Color.gray));
    final int outerLine = 5;
    final int gap = 10;
    double fill = TableLayout.FILL;
    double[] cols = {outerLine, fill, outerLine};
    double[] rows = {outerLine,
                     TableLayout.PREFERRED, //Title
                     gap,
                     fill, //Content
                     outerLine};

    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(0, 0, new MouseSensor(new MouseDragHandler(pWindow, Cursor.NW_RESIZE_CURSOR)));
    tlu.add(1, 0, new MouseSensor(new MouseDragHandler(pWindow, Cursor.N_RESIZE_CURSOR)));
    tlu.add(2, 0, new MouseSensor(new MouseDragHandler(pWindow, Cursor.NE_RESIZE_CURSOR)));
    tlu.add(0, 1, 0, 3, new MouseSensor(new MouseDragHandler(pWindow, Cursor.W_RESIZE_CURSOR)));
    tlu.add(1, 1, new TitlePanel(new HandlerMovement(pWindow), pTitle));
    tlu.add(2, 1, 2, 3, new MouseSensor(new MouseDragHandler(pWindow, Cursor.E_RESIZE_CURSOR)));
    tlu.add(1, 2, new MouseSensor(new HandlerMovement(pWindow)));
    tlu.add(1, 3, _createContentPanel(pComponent));
    tlu.add(0, 4, new MouseSensor(new MouseDragHandler(pWindow, Cursor.SW_RESIZE_CURSOR)));
    tlu.add(1, 4, new MouseSensor(new MouseDragHandler(pWindow, Cursor.S_RESIZE_CURSOR)));
    tlu.add(2, 4, new MouseSensor(new MouseDragHandler(pWindow, Cursor.SE_RESIZE_CURSOR)));
  }

  private JComponent _createContentPanel(JComponent pComponent)
  {
    JScrollPane contentScrollPane = new JScrollPane(pComponent);
    contentScrollPane.setPreferredSize(new Dimension(pComponent.getPreferredSize().width, 600));
    contentScrollPane.getVerticalScrollBar().setUnitIncrement(16);

    contentScrollPane.setViewportBorder(null);
    contentScrollPane.getViewport().setBorder(null);
    contentScrollPane.setBorder(null);
    return contentScrollPane;
  }

}