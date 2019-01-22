package de.adito.git.gui;

import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import java.awt.*;

/**
 * HighlightPainter for Highlights that always paints the whole width of the textField/Pane, not just the area below the text
 * For the 3 different modes see the descriptions in Mode
 *
 * @author m.kaspera, 16.01.2019
 */
public class LineHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter
{

  private static final int THIN_LINE_HEIGHT = 3;

  private final Color color;
  private final Mode mode;

  public enum Mode
  {
    /**
     * Highlight the complete line(s) in one continuous color
     */
    WHOLE_LINE,
    /**
     * Only draw a thin line over the text line (if several lines lie in the specified offsets, draw a thin line over the first line only)
     */
    THIN_LINE,
    /**
     * Highlight a part of the line in the given Color and use a lighter version of the color to mark the rest of the line
     */
    MARK_PART
  }

  /**
   * Constructs a new highlight painter. If <code>c</code> is null,
   * the JTextComponent will be queried for its selection color.
   *
   * @param pColor the color for the highlight
   */
  LineHighlightPainter(Color pColor, Mode pMode)
  {
    super(pColor);
    color = pColor;
    mode = pMode;
  }

  @Override
  public void paint(Graphics pGraphics, int pOffs0, int pOffs1, Shape pBounds, JTextComponent pComponent)
  {
    try
    {
      // --- determine locations ---
      TextUI mapper = pComponent.getUI();
      Rectangle p0 = mapper.modelToView(pComponent, pOffs0);
      Rectangle p1 = mapper.modelToView(pComponent, pOffs1);
      pGraphics.setColor(color);
      // start and end are in the same line, so only color that particular line
      if (p0.y == p1.y)
      {
        if (mode == Mode.MARK_PART)
        {
          // draw whole line in bright background
          pGraphics.setColor(LineHighlightPainter._darken(color));
          pGraphics.fillRect(pComponent.getInsets().left, p0.y, pComponent.getWidth(), p0.height);

        }
        else
        {
          int height = p0.height;
          // only need a line with 1px if Mode.THIN_LINE
          if (mode == Mode.THIN_LINE)
          {
            height = THIN_LINE_HEIGHT;
            // center the line between the two text lines
            p0.y = p0.y - THIN_LINE_HEIGHT / 2;
          }
          pGraphics.fillRect(pComponent.getInsets().left, p0.y, pComponent.getWidth(), height);
        }
      }
      else
      {
        // height of the rectangle to draw is from the top of p0 to the bottom of p1
        int height = (p1.y + p1.height) - p0.y;
        // only need a line with THIN_LINE_HEIGHT if Mode.THIN_LINE
        if (mode == Mode.THIN_LINE)
        {
          height = THIN_LINE_HEIGHT;
          // center the line between the two text lines
          p0.y = p0.y - THIN_LINE_HEIGHT / 2;
        }
        pGraphics.fillRect(pComponent.getInsets().left, p0.y, pComponent.getWidth(), height);
      }
    }
    catch (BadLocationException pE)
    {
      // can't render
    }
  }

  @Override
  public Shape paintLayer(Graphics pGraphics, int pOffs0, int pOffs1, Shape pBounds, JTextComponent pComponent, View pView)
  {
    pGraphics.setColor(color);
    Rectangle r;

    if (pOffs0 == pView.getStartOffset() &&
        pOffs1 == pView.getEndOffset())
    {
      // Contained in view, can just use bounds.
      if (pBounds instanceof Rectangle)
      {
        r = (Rectangle) pBounds;
      }
      else
      {
        r = pBounds.getBounds();
      }
    }
    else
    {
      // Should only render part of View.
      try
      {
        // --- determine locations ---
        Shape shape = pView.modelToView(pOffs0, Position.Bias.Forward,
                                        pOffs1, Position.Bias.Backward,
                                        pBounds);
        r = (shape instanceof Rectangle) ?
            (Rectangle) shape : shape.getBounds();
      }
      catch (BadLocationException e)
      {
        // can't render
        r = null;
      }
    }

    if (r != null)
    {
      // if mode is thin line, the height must be limited to THIN_LINE_HEIGHT
      if (mode == Mode.THIN_LINE)
      {
        r.height = THIN_LINE_HEIGHT;
        // center the line between the two text lines
        r.y = r.y - THIN_LINE_HEIGHT / 2;
      }

      // If we are asked to highlight, we should draw something even
      // if the model-to-view projection is of zero width (6340106).
      r.width = Math.max(r.width, 1);

      pGraphics.fillRect(r.x, r.y, r.width, r.height);
    }

    return r;
  }

  /**
   * @param pColor Color to get in a bit darker
   * @return Color whose brightness value is scaled down by an algorithm
   */
  private static Color _darken(Color pColor)
  {
    // some random factor that seemed to work well in tests, same goes for the algorithm
    float factor = 0.9f;
    float[] hsbValues = new float[3];
    Color.RGBtoHSB(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), hsbValues);
    if (hsbValues[2] == 0)
    {
      // equation as-is without the multiplication with 0 that would result if you insert 0
      hsbValues[2] = 0.5f * factor;
    }
    else if (hsbValues[2] == 0.5f)
    {
      // equation as-is without the multiplication with 0 that would result if you insert 0.5
      hsbValues[2] = factor * hsbValues[2];
    }
    else
    {
      hsbValues[2] = Math.abs(0.5f - hsbValues[2]) * factor * hsbValues[2];
    }
    return Color.getHSBColor(hsbValues[0], hsbValues[1], hsbValues[2]);
  }
}
