package de.adito.git.gui;

import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author m.kaspera 08.11.2018
 */
public class LineHighlighter extends DefaultHighlighter
{

  private Map<Object, LineHighlighter._Highlight> backgroundHighlights = new LinkedHashMap<>();
  private JTextComponent component;

  /**
   * @see javax.swing.text.DefaultHighlighter#install(javax.swing.text.JTextComponent)
   */
  @Override
  public final void install(final JTextComponent pC)
  {
    super.install(pC);
    this.component = pC;
  }

  /**
   * @see javax.swing.text.DefaultHighlighter#deinstall(javax.swing.text.JTextComponent)
   */
  @Override
  public final void deinstall(final JTextComponent pC)
  {
    super.deinstall(pC);
    this.component = null;
  }

  @Override
  public void paint(Graphics pG)
  {
    final Highlighter.Highlight[] highlights = getHighlights();
    final int len = highlights.length;
    for (int i = 0; i < len; i++)
    {
      Highlighter.Highlight info = highlights[i];
      if (info.getClass().getName().contains("LayeredHighlightInfo"))
      {
        // Avoid allocing unless we need it.
        final Rectangle a = this.component.getBounds();
        final Insets insets = this.component.getInsets();
        a.x = insets.left;
        a.y = insets.top;
        a.height -= insets.top + insets.bottom;
        for (; i < len; i++)
        {
          info = highlights[i];
          if (info.getClass().getName().contains("LayeredHighlightInfo"))
          {
            final Highlighter.HighlightPainter p = info
                .getPainter();
            p.paint(pG, info.getStartOffset(), info
                .getEndOffset(), a, this.component);
          }
        }
      }
    }
  }

  @Override
  public Object addHighlight(int pP0, int pP1, HighlightPainter pP) throws BadLocationException
  {
    Object tag = super.addHighlight(pP0, pP1, pP);
    _sortHighlights();
    return tag;
  }

  Object addBackgroundHighlight(int pP0, int pP1, HighlightPainter pP) throws BadLocationException
  {
    Object tag = super.addHighlight(pP0, pP1, pP);
    backgroundHighlights.put(tag, new LineHighlighter._Highlight(pP0, pP1, pP));
    return tag;
  }

  @Override
  public void removeHighlight(Object pTag)
  {
    super.removeHighlight(pTag);
    backgroundHighlights.remove(pTag);
    component.repaint();
  }

  @Override
  public void removeAllHighlights()
  {
    super.removeAllHighlights();
    backgroundHighlights.clear();
  }

  private void _sortHighlights() throws BadLocationException
  {
    List<LineHighlighter._Highlight> bgHighlights = new ArrayList<>(backgroundHighlights.values());
    new ArrayList<>(backgroundHighlights.keySet()).forEach(this::removeHighlight);
    for (LineHighlighter._Highlight highlight : bgHighlights)
    {
      addBackgroundHighlight(highlight.start, highlight.end, highlight.painter);
    }
  }

  private static class _Highlight
  {
    private int start;
    private int end;
    private HighlightPainter painter;

    private _Highlight(int start, int end, HighlightPainter painter)
    {
      this.start = start;
      this.end = end;
      this.painter = painter;
    }
  }
}