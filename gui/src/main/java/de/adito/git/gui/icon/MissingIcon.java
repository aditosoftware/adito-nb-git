package de.adito.git.gui.icon;

import javax.swing.*;
import java.awt.*;

/**
 * Icon that may be used if no icon was found, the icon itself is just a red cross on transparent background
 * Defined here so it is assured that the icon can be created
 *
 * @author m.kaspera, 13.08.2019
 */
public class MissingIcon implements Icon
{

  private final int width;
  private final int height;
  private final BasicStroke stroke = new BasicStroke(4);

  private MissingIcon(int pWidth, int pHeight)
  {
    width = pWidth;
    height = pHeight;
  }

  /**
   * create a MissingIcon of size 16x16
   *
   * @return Icon of size 16x16
   */
  public static MissingIcon get16x16()
  {
    return new MissingIcon(16, 16);
  }

  /**
   * create a MissingIcon of size 32x32
   *
   * @return icon of size 32x32
   */
  public static MissingIcon get32x32()
  {
    return new MissingIcon(32, 32);
  }

  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    Graphics2D g2d = (Graphics2D) g.create();


    g2d.setColor(Color.RED);
    g2d.setStroke(stroke);

    int distanceFromBorder = (int) (width * 0.3);
    g2d.drawLine(x + distanceFromBorder, y + distanceFromBorder, x + width - distanceFromBorder, y + height - distanceFromBorder);
    g2d.drawLine(x + distanceFromBorder, y + height - distanceFromBorder, x + width - distanceFromBorder, y + distanceFromBorder);

    g2d.dispose();
  }

  public int getIconWidth()
  {
    return width;
  }

  public int getIconHeight()
  {
    return height;
  }
}
