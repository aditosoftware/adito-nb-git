package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import java.awt.Color;

/**
 * Describes a highlight on a scrollbar
 *
 * @author m.kaspera, 25.01.2019
 */
class ScrollbarMarking
{

  private final int yCoordinate;
  private final int extent;
  private final Color color;

  ScrollbarMarking(int pYCoordinate, int pExtent, Color pColor)
  {
    yCoordinate = pYCoordinate;
    extent = pExtent;
    color = pColor;
  }

  /**
   * @return y coordinate of the upper left corner of the area to be drawn, in view coordinates
   */
  int getYCoordinate()
  {
    return yCoordinate;
  }

  /**
   * @return height of the area to be drawn, in view coordinates
   */
  int getExtent()
  {
    return extent;
  }

  /**
   * @return Color to fill the marking with
   */
  public Color getColor()
  {
    return color;
  }
}
