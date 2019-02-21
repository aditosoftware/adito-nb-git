package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * Represents one of the colored areas beneath the line numbers
 *
 * @author m.kaspera, 15.01.2019
 */
class LineNumberColor
{
  private Color color;
  private Rectangle coloredArea;

  LineNumberColor(Color pColor, Rectangle pColoredArea)
  {
    color = pColor;
    coloredArea = pColoredArea;
  }

  /**
   * @return Color that the area should be colored in
   */
  Color getColor()
  {
    return color;
  }

  /**
   * @return Area that should be colored as Rectangle
   */
  Rectangle getColoredArea()
  {
    return coloredArea;
  }
}
