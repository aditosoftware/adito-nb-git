package de.adito.git.gui;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Handles inserting lists of IFileChangeChunks into textPanes and highlighting parts of the text
 * The highlighted parts are determined by the IFileChangeChunks, and the highlighting is done
 * in a layered manner, so text selection is still possible
 *
 * @author m.kaspera 22.10.2018
 */
public class TextHighlightUtil
{

  private TextHighlightUtil()
  {
  }

  /**
   * @param pEditorPane       JEditorPane that should be filled with text and colored
   * @param pFileChangeChunks IFileChangeChunks determining the text and highlighting
   * @param pChangeSide       which side of a IFileChangeChunk should be taken
   */
  public static void insertColoredText(JEditorPane pEditorPane, List<IFileChangeChunk> pFileChangeChunks, EChangeSide pChangeSide)
  {
    _insertColoredText(pEditorPane, pFileChangeChunks, pChangeSide, ArrayList::new);
  }

  /**
   * Combines the highlighting of two List of IFileChangeChunks, text is assumed to be identical
   *
   * @param pEditorPane            JEditorPane that should be filled with text and colored
   * @param pYourFileChangeChunks  IFileChangeChunks determining the text and highlighting
   * @param pTheirFileChangeChunks IFileChangeChunks determining the text and highlighting
   * @param pChangeSide            which side of a IFileChangeChunk should be taken
   */
  public static void insertColoredText(JEditorPane pEditorPane, List<IFileChangeChunk> pYourFileChangeChunks,
                                       List<IFileChangeChunk> pTheirFileChangeChunks, EChangeSide pChangeSide)
  {
    _insertColoredText(pEditorPane, pYourFileChangeChunks, pChangeSide,
                       () -> _getHighlightSpots(pEditorPane, pTheirFileChangeChunks, pChangeSide));
  }

  /**
   * @param pEditorPane       JEditorPane that should be filled with text and colored
   * @param pFileChangeChunks List of IFileChangeChunks for which the highlighted areas should be determined
   * @param pChangeSide       which side of a IFileChangeChunk should be taken
   * @return List of _Highlight
   */
  private static List<_Highlight> _getHighlightSpots(JEditorPane pEditorPane,
                                                     List<IFileChangeChunk> pFileChangeChunks, EChangeSide pChangeSide)
  {
    List<_Highlight> highlightSpots = new ArrayList<>();
    for (IFileChangeChunk changeChunk : pFileChangeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME)
      {
        // use maxEditorLineIndex to min(maxEditorLineIndex, x) to make sure no oOBException occurs, better to have a wrong result before update
        int maxEditorLineIndex = pEditorPane.getDocument().getDefaultRootElement().getElementCount() - 1;
        int startOffset = pEditorPane.getDocument().getDefaultRootElement()
            .getElement(Math.min(maxEditorLineIndex, changeChunk.getStart(pChangeSide)))
            .getStartOffset();
        // -1 because the end line is considered exclusive (also if endLine == startLine the offsets are the same this way).
        // Edge case here: empty file -> endLine - 1 would result in -1, so need to use Math.max(0, endLine - 1)
        int endOffset = pEditorPane.getDocument().getDefaultRootElement()
            .getElement(Math.min(maxEditorLineIndex, Math.max(0, changeChunk.getEnd(pChangeSide) - 1)))
            .getEndOffset();
        // endOffset is considered the next line, so unless endOffset and startOffset are the same subtract 1 so the next line is not colored as well
        if (startOffset < endOffset)
          endOffset -= 1;
        highlightSpots.add(new _Highlight(startOffset, endOffset,
                                          new LineHighlightPainter(changeChunk.getChangeType().getDiffColor(),
                                                                   startOffset == endOffset ? LineHighlightPainter.Mode.THIN_LINE
                                                                       : LineHighlightPainter.Mode.WHOLE_LINE)));
      }
    }
    return highlightSpots;
  }

  /**
   * @param pJEditorPane                  JEditorPane that should be filled with text and colored
   * @param pFileChangeChunks             List of IFileChangeChunks providing the text and the information about which areas to highlight
   * @param pChangeSide                   which side of a IFileChangeChunk should be taken
   * @param pAdditionalHighlightsSupplier Supplier of list of _Highlight determining which additional areas get colored and the color of the areas
   */
  private static void _insertColoredText(JEditorPane pJEditorPane, List<IFileChangeChunk> pFileChangeChunks,
                                         EChangeSide pChangeSide, Supplier<List<_Highlight>> pAdditionalHighlightsSupplier)
  {
    StringBuilder paneContentBuilder = new StringBuilder();
    for (IFileChangeChunk changeChunk : pFileChangeChunks)
    {
      paneContentBuilder.append(changeChunk.getLines(pChangeSide));
      paneContentBuilder.append(changeChunk.getParityLines(pChangeSide));
    }
    pJEditorPane.setText(paneContentBuilder.toString());
    int numParityLines = 0;
    List<_Highlight> highlights = pAdditionalHighlightsSupplier.get();
    for (IFileChangeChunk changeChunk : pFileChangeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME)
      {
        // use maxEditorLineIndex to min(maxEditorLineIndex, x) to make sure no oOBException occurs, better to have a wrong result before update
        int maxEditorLineIndex = pJEditorPane.getDocument().getDefaultRootElement().getElementCount() - 1;
        int startOffset = pJEditorPane.getDocument().getDefaultRootElement()
            .getElement(Math.min(maxEditorLineIndex, changeChunk.getStart(pChangeSide) + numParityLines)).getStartOffset();
        numParityLines += changeChunk.getParityLines(pChangeSide).length();
        // Minus one in the getElement() because the last line is not included. In an empty file this would be -1, so use Math.max(0, x)
        int endOffset = pJEditorPane.getDocument().getDefaultRootElement()
            .getElement(
                Math.min(maxEditorLineIndex,
                         Math.max(0, changeChunk.getEnd(pChangeSide) + numParityLines - 1)))
            .getEndOffset();
        // endOffset is considered the next line, so unless endOffset and startOffset are the same subtract 1 so the next line is not colored as well
        if (startOffset < endOffset)
          endOffset -= 1;
        highlights.add(new _Highlight(startOffset, endOffset,
                                      new LineHighlightPainter(changeChunk.getChangeType().getDiffColor(),
                                                               startOffset == endOffset ? LineHighlightPainter.Mode.THIN_LINE
                                                                   : LineHighlightPainter.Mode.WHOLE_LINE)));
      }
    }
    _colorHighlights(pJEditorPane, highlights);
  }

  private static void _colorHighlights(JEditorPane pJEditorPane, List<_Highlight> pHighlightSpots)
  {
    LineHighlighter highlighter = new LineHighlighter();
    pJEditorPane.setHighlighter(highlighter);
    try
    {
      for (_Highlight highlight : pHighlightSpots)
      {
        highlighter.addBackgroundHighlight(highlight.getStartIndex(), highlight.getEndOffset(), highlight.getPainter());
      }
    }
    catch (BadLocationException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * class to store the diverse spots that should be highlighted after
   * the IFileChangeChunks are inserted.
   */
  private static class _Highlight
  {

    private final int startIndex;
    private final int endOffset;
    private final DefaultHighlighter.DefaultHighlightPainter painter;

    _Highlight(int startIndex, int endOffset, LineHighlightPainter painter)
    {
      this.startIndex = startIndex;
      this.endOffset = endOffset;
      this.painter = painter;
    }

    int getStartIndex()
    {
      return startIndex;
    }

    int getEndOffset()
    {
      return endOffset;
    }

    DefaultHighlighter.DefaultHighlightPainter getPainter()
    {
      return painter;
    }
  }
}
