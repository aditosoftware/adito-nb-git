package de.adito.git.gui;

import de.adito.git.api.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
   * @param pFileChangesEvent IFileChangesEvent that has the current list of IFileChangeChunks and the descriptions about the changes that have to
   *                          happen to the contents of the editor to keep it in sync with the list of IFileChangeChunks, without throwing away the
   *                          complete text
   * @param pChangeSide       which side of a IFileChangeChunk should be taken
   */
  public static void insertColoredText(JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, EChangeSide pChangeSide)
  {
    _insertColoredText(pEditorPane, pFileChangesEvent, pChangeSide, () -> _getHighlightSpots(pEditorPane, pFileChangesEvent.getNewValue(),
                                                                                             pChangeSide));
  }

  /**
   * Combines the highlighting of two List of IFileChangeChunks, text is assumed to be identical
   *
   * @param pEditorPane            JEditorPane that should be filled with text and colored
   * @param pYourFileChangesEvent  IFileChangeChunks determining the text and highlighting
   * @param pTheirFileChangesEvent IFileChangeChunks determining the text and highlighting
   * @param pChangeSide            which side of a IFileChangeChunk should be taken
   */
  public static void insertColoredText(JEditorPane pEditorPane, IFileChangesEvent pYourFileChangesEvent,
                                       IFileChangesEvent pTheirFileChangesEvent, EChangeSide pChangeSide)
  {
    IFileChangesEvent passOnEvent = pYourFileChangesEvent;
    if (pYourFileChangesEvent.getEditorChange() == null ||
        (pTheirFileChangesEvent.getEditorChange() != null
            && pTheirFileChangesEvent.getEditorChange().getChange(pChangeSide).getType() != EChangeType.SAME
            && pTheirFileChangesEvent.getEditorChange().getChange(EChangeSide.OLD).getLength()
            < pYourFileChangesEvent.getEditorChange().getChange(EChangeSide.OLD).getLength()))
      passOnEvent = pTheirFileChangesEvent;
    _insertColoredText(pEditorPane, passOnEvent, pChangeSide,
                       () -> {
                         List<_Highlight> highlightSpots = _getHighlightSpots(pEditorPane, pTheirFileChangesEvent.getNewValue(), pChangeSide);
                         highlightSpots.addAll(_getHighlightSpots(pEditorPane, pYourFileChangesEvent.getNewValue(), pChangeSide));
                         return highlightSpots;
                       });
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
   * @param pJEditorPane        JEditorPane that should be filled with text and colored
   * @param pFileChangesEvent   IFileChangesEvent that has the current list of IFileChangeChunks and the descriptions about the changes that have to
   *                            happen to the contents of the editor to keep it in sync with the list of IFileChangeChunks, without throwing away the
   *                            complete text
   * @param pChangeSide         which side of a IFileChangeChunk should be taken
   * @param pHighlightsSupplier Supplier of list of _Highlight determining which additional areas get colored and the color of the areas
   *                            This is a supplier instead of a list because the text has to be inserted before the highlights are calculated
   */
  private static void _insertColoredText(JEditorPane pJEditorPane, IFileChangesEvent pFileChangesEvent,
                                         EChangeSide pChangeSide, Supplier<List<_Highlight>> pHighlightsSupplier)
  {
    try
    {
      if (pFileChangesEvent.getEditorChange() != null)
      {
        IEditorChange editorChange = pFileChangesEvent.getEditorChange().getChange(pChangeSide);
        if (editorChange.getType() == EChangeType.DELETE || editorChange.getType() == EChangeType.MODIFY)
        {
          pJEditorPane.getDocument().remove(editorChange.getOffset(), editorChange.getLength());
        }
        if (editorChange.getType() == EChangeType.MODIFY || editorChange.getType() == EChangeType.ADD)
        {
          pJEditorPane.getDocument().insertString(editorChange.getOffset(), _cleanString(editorChange.getText()), null);
        }
      }
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    _colorHighlights(pJEditorPane, pHighlightsSupplier.get());
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
   * Make the newlines in the string uniform \n
   *
   * @param pUnCleanString String that should be cleaned such that the newlines are always only \n
   * @return String with only \n as newlines
   */
  @NotNull
  private static String _cleanString(@Nullable String pUnCleanString)
  {
    if (pUnCleanString != null)
    {
      if (pUnCleanString.contains("\n"))
        pUnCleanString = pUnCleanString.replace("\r", "");
      else
        pUnCleanString = pUnCleanString.replace("\r", "\n");
      return pUnCleanString;
    }
    return "";
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
