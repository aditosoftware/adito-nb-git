package de.adito.git.gui;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author m.kaspera 08.11.2018
 */
public class LineHighlighter extends DefaultHighlighter {

    private Map<Object, LineHighlighter._Highlight> backgroundHighlights = new LinkedHashMap<>();
    private JTextComponent component;

    public LineHighlighter() {
    }


    /**
     * @see javax.swing.text.DefaultHighlighter#install(javax.swing.text.JTextComponent)
     */
    @Override
    public final void install(final JTextComponent c) {
        super.install(c);
        this.component = c;
    }

    /**
     * @see javax.swing.text.DefaultHighlighter#deinstall(javax.swing.text.JTextComponent)
     */
    @Override
    public final void deinstall(final JTextComponent c) {
        super.deinstall(c);
        this.component = null;
    }

    @Override
    public void paint(Graphics g) {
        final Highlighter.Highlight[] highlights = getHighlights();
        final int len = highlights.length;
        for (int i = 0; i < len; i++) {
            Highlighter.Highlight info = highlights[i];
            if (info.getClass().getName().contains("LayeredHighlightInfo")) {
                // Avoid allocing unless we need it.
                final Rectangle a = this.component.getBounds();
                final Insets insets = this.component.getInsets();
                a.x = insets.left;
                a.y = insets.top;
                // a.width -= insets.left + insets.right + 100;
                a.height -= insets.top + insets.bottom;
                for (; i < len; i++) {
                    info = highlights[i];
                    if (info.getClass().getName().contains("LayeredHighlightInfo")) {
                        final Highlighter.HighlightPainter p = info
                                .getPainter();
                        p.paint(g, info.getStartOffset(), info
                                .getEndOffset(), a, this.component);
                    }
                }
            }
        }
    }

    @Override
    public Object addHighlight(int p0, int p1, HighlightPainter p) throws BadLocationException {
        Object tag = super.addHighlight(p0, p1, p);
        _sortHighlights();
        return tag;
    }

    public Object addBackgroundHighlight(int p0, int p1, HighlightPainter p) throws BadLocationException {
        Object tag = super.addHighlight(p0, p1, p);
        backgroundHighlights.put(tag, new LineHighlighter._Highlight(p0, p1, p));
        return tag;
    }

    @Override
    public void removeHighlight(Object tag) {
        super.removeHighlight(tag);
        backgroundHighlights.remove(tag);
        component.repaint();
    }

    @Override
    public void removeAllHighlights() {
        super.removeAllHighlights();
        backgroundHighlights.clear();
    }

    private void _sortHighlights() throws BadLocationException {
        List<LineHighlighter._Highlight> bgHighlights = new ArrayList<>(backgroundHighlights.values());
        new ArrayList<>(backgroundHighlights.keySet()).forEach(this::removeHighlight);
        for (LineHighlighter._Highlight highlight : bgHighlights) {
            addBackgroundHighlight(highlight.start, highlight.end, highlight.painter);
        }
    }

    private static class _Highlight {
        private int start;
        private int end;
        private HighlightPainter painter;

        private _Highlight(int start, int end, HighlightPainter painter) {
            this.start = start;
            this.end = end;
            this.painter = painter;
        }
    }
}