package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Scrollbar that has markings drawn on it
 *
 * @author m.kaspera, 25.01.2019
 */
public class MarkedScrollbar extends JScrollBar
{

  private static final int MARKINGS_OPACITY = 150;
  private static final int MIN_MARKING_HEIGHT = 3;
  private List<ScrollbarMarking> markings = new ArrayList<>();
  private BufferedImage bufferedImage;

  public MarkedScrollbar(List<ScrollbarMarking> pMarkings)
  {
    markings = pMarkings;
    bufferedImage = _calculateImage(markings);
  }

  public MarkedScrollbar(int orientation, int value, int extent, int min, int max)
  {
    super(orientation, value, extent, min, max);
  }

  public MarkedScrollbar(int orientation)
  {
    super(orientation);
  }

  public MarkedScrollbar()
  {
    super();
  }

  /**
   * @param pMarkings List of ScrollbarMarkings that should be drawn over the Scrollbar. Marking coordinates are in the view coordinate system
   */
  void setMarkings(List<ScrollbarMarking> pMarkings)
  {
    markings = pMarkings;
    bufferedImage = _calculateImage(markings);
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    if (bufferedImage == null && !markings.isEmpty())
      bufferedImage = _calculateImage(markings);
    if (bufferedImage != null)
      pGraphics.drawImage(bufferedImage, 0, 0, null);
  }

  /**
   * Calculates the BufferedImage that is drawn over the Scrollbar, based on the passed markings
   *
   * @param pMarkings List of ScrollbarMarkings that should be drawn over the Scrollbar. Marking coordinates are in the view coordinate system
   * @return BufferedImage to be drawn over the Scrollbar
   */
  private BufferedImage _calculateImage(List<ScrollbarMarking> pMarkings)
  {
    if (this.getHeight() <= 0 || this.getWidth() <= 0)
      return null;
    int scrollbarHeight = this.getHeight();
    int maxValue = this.getMaximum();
    double distortionFactor = scrollbarHeight / (double) maxValue;
    BufferedImage image = new BufferedImage(this.getWidth(), scrollbarHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics graphics = image.getGraphics();
    for (ScrollbarMarking marking : pMarkings)
    {
      int paintFrom = (int) (marking.getYCoordinate() * distortionFactor);
      // if the maxValue is very high and the marking small this could be 0 height without the Math.max (should always be shown though)
      int paintHeight = Math.max(MIN_MARKING_HEIGHT, (int) (marking.getExtent() * distortionFactor));
      // set an alpha value so if a marking takes a big part of the scrollbar, the current position is still visible through the marking
      graphics.setColor(new Color(marking.getColor().getRed(), marking.getColor().getGreen(), marking.getColor().getBlue(), MARKINGS_OPACITY));
      graphics.fillRect(0, paintFrom, this.getWidth(), paintHeight);
    }
    return image;
  }
}
