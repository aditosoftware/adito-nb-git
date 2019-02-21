package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import java.awt.Color;
import java.awt.Polygon;

/**
 * Connection between two changed chunks of neighboring LineNumPanels
 *
 * @author m.kaspera, 21.01.2019
 */
class ChangedChunkConnection
{

  private final Polygon shape;
  private final Color color;

  ChangedChunkConnection(Polygon pShape, Color pColor)
  {
    shape = pShape;
    color = pColor;
  }

  Polygon getShape()
  {
    return shape;
  }

  Color getColor()
  {
    return color;
  }
}
