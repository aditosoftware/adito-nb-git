package de.adito.git.gui.popup;

import javax.swing.*;
import java.awt.Color;

/**
 * Mouse provider for the MouseListener
 *
 * @author a.arnold, 15.11.2018
 */
class MouseSensor extends JPanel
{

  /**
   * @param pDragHandler DragHandler that should be added as mouse and mousemotionListener
   */
  MouseSensor(MouseDragHandler pDragHandler)
  {
    addMouseListener(pDragHandler);
    addMouseMotionListener(pDragHandler);
  }

  /**
   * @param pDragHandler     DragHandler that should be added as mouse and mousemotionListener
   * @param pBackgroundColor color used as background for this panel
   */
  MouseSensor(MouseDragHandler pDragHandler, Color pBackgroundColor)
  {
    this(pDragHandler);
    setBackground(pBackgroundColor);
  }
}
