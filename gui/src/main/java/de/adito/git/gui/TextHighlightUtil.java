package de.adito.git.gui;

import de.adito.git.api.data.diff.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Handles IDeltaTextChangeEvent that describe changes to the contents of a given editorPane. The IDeltaTextChangeEvents also contain IChangeDeltas that determine
 * the parts of the text that should be highlighted. The highlighting is done in a layered manner, so text selection is still possible
 *
 * @author m.kaspera 22.10.2018
 */
public class TextHighlightUtil
{

  private TextHighlightUtil()
  {
  }

  /**
   * @param pEditorPane      JEditorPane that should be filled with text and colored
   * @param pTextChangeEvent IDeltaTextChangeEvent that has the current list of IChangeDeltas and the descriptions about the changes that have to
   *                         happen to the contents of the editor to keep it in sync, without throwing away the complete text
   * @param pChangeSide      which side of a IDeltaTextChangeEvent and IChangeDeltas should be taken
   */
  public static void insertColoredText(JEditorPane pEditorPane, IDeltaTextChangeEvent pTextChangeEvent, EChangeSide pChangeSide)
  {
    List<IChangeDelta> changeDeltas = pTextChangeEvent.getFileDiff() == null ? List.of() : pTextChangeEvent.getFileDiff().getChangeDeltas();
    if (pTextChangeEvent.getSide() == pChangeSide)
    {
      _insertColoredText(pEditorPane, pTextChangeEvent, () -> _getHighlightSpots(changeDeltas, pChangeSide, true));
    }
    else
    {
      _colorHighlights(pEditorPane, _getHighlightSpots(changeDeltas, pChangeSide, true));
    }
  }

  /**
   * Combines the highlighting of IDeltaTextChangeEvent, text of the used EChangeSide is assumed to be identical
   *
   * @param pEditorPane JEditorPane that should be filled with text and colored
   * @param pChangeSide which side of the IDeltaTextChangeEvents should be taken
   */
  public static void insertColoredText(JEditorPane pEditorPane, IDeltaTextChangeEvent pYourChangeEvent, IDeltaTextChangeEvent pTheirChangeEvent, EChangeSide pChangeSide)
  {
    IDeltaTextChangeEvent realChangeEvent = _getRealEvent(pYourChangeEvent, pTheirChangeEvent);
    if (realChangeEvent.getSide() != pChangeSide)
      return;
    List<IChangeDelta> changeDeltas = new ArrayList<>();
    changeDeltas.addAll(pYourChangeEvent.getFileDiff() == null ? List.of() : pYourChangeEvent.getFileDiff().getChangeDeltas());
    changeDeltas.addAll(pTheirChangeEvent.getFileDiff() == null ? List.of() : pTheirChangeEvent.getFileDiff().getChangeDeltas());
    _insertColoredText(pEditorPane, realChangeEvent, () -> _getHighlightSpots(changeDeltas, pChangeSide, true)
    );
  }

  private static IDeltaTextChangeEvent _getRealEvent(IDeltaTextChangeEvent pYourChangeEvent, IDeltaTextChangeEvent pTheirChangeEvent)
  {
    if ((pYourChangeEvent.getText() == null || pYourChangeEvent.getText().isEmpty()) && pYourChangeEvent.getOffset() == 0 && pYourChangeEvent.getLength() == 0)
      return pTheirChangeEvent;
    else
      return pYourChangeEvent;
  }

  /**
   * @param pFileChangeChunks List of IChangeDelta for which the highlighted areas should be determined
   * @param pChangeSide       which side of a IChangeDelta should be used
   * @return List of _Highlight
   */
  // TODO: pIsMarkWords is always true, get a real value from some kind of setting
  private static List<_Highlight> _getHighlightSpots(List<IChangeDelta> pFileChangeChunks, EChangeSide pChangeSide, boolean pIsMarkWords)
  {
    List<_Highlight> highlightSpots = new ArrayList<>();
    List<_Highlight> pendingHighlightSpots = new ArrayList<>();
    for (IChangeDelta changeDelta : pFileChangeChunks)
    {
      if (changeDelta.getChangeType() == EChangeType.DELETE && changeDelta.getEndTextIndex(EChangeSide.OLD) == changeDelta.getText(EChangeSide.OLD).length())
      {
        _getLineHighlight(changeDelta, pChangeSide, false, highlightSpots);
      }
      else if (pIsMarkWords && changeDelta.getChangeStatus() == EChangeStatus.PENDING && (changeDelta.getChangeType() == EChangeType.MODIFY))
      {
        _getWordsHighlight(changeDelta, pChangeSide, highlightSpots, pendingHighlightSpots);
      }
      else
      {
        if (changeDelta.getChangeStatus() == EChangeStatus.PENDING)
          _getLineHighlight(changeDelta, pChangeSide, true, pendingHighlightSpots);
        _getLineHighlight(changeDelta, pChangeSide, true, highlightSpots);
      }
    }
    highlightSpots.addAll(pendingHighlightSpots);
    return highlightSpots;
  }

  /**
   * Get the _Highlight object encompassing the whole change for the given IChangeDelta
   *
   * @param changeDelta IChangeDelta for which the _Highlight object should be created
   * @param pChangeSide which side of a IChangeDelta should be used
   * @param pHighlights List of _Highlights that the line highlight should be added to
   */
  private static void _getLineHighlight(IChangeDelta changeDelta, EChangeSide pChangeSide, boolean pUsePrimaryDiffColor, List<_Highlight> pHighlights)
  {
    int startOffset = changeDelta.getStartTextIndex(pChangeSide);
    int endOffset = changeDelta.getEndTextIndex(pChangeSide);
    if (startOffset < endOffset)
      endOffset -= 1;
    Color highlightColor = pUsePrimaryDiffColor ? changeDelta.getDiffColor() : changeDelta.getSecondaryDiffColor();
    pHighlights.add(new _Highlight(new _HighlightSpot(startOffset, endOffset, highlightColor),
                                   _isUseThinLine(changeDelta, pChangeSide) ? LineHighlightPainter.Mode.THIN_LINE : LineHighlightPainter.Mode.WHOLE_LINE));
  }

  /**
   * Get the _Highlight for the given IChangeDelta. The highlight here marks the whole change in a background color and separately highlights the
   * differences on a word-basis
   *
   * @param changeDelta            IChangeDelta for which the _Highlight object should be created
   * @param pChangeSide            which side of a IChangeDelta should be used
   * @param pHighlightSpots        List of _Highlights to which to add all secondary highlights (drawn first)
   * @param pPendingHighlightSpots List of _Highlights to which to add all primary highlights (drawn second -> in foreground)
   */
  private static void _getWordsHighlight(IChangeDelta changeDelta, EChangeSide pChangeSide, List<_Highlight> pHighlightSpots, List<_Highlight> pPendingHighlightSpots)
  {
    for (ILinePartChangeDelta linePartChangeDelta : changeDelta.getLinePartChanges())
    {
      if (_isMarkDelta(pChangeSide, linePartChangeDelta))
      {
        int startOffset = linePartChangeDelta.getStartTextIndex(pChangeSide);
        int endOffset = linePartChangeDelta.getEndTextIndex(pChangeSide);
        if (startOffset < endOffset)
          endOffset -= 1;
        pPendingHighlightSpots.add(new _Highlight(new _HighlightSpot(startOffset, endOffset, changeDelta.getDiffColor()), LineHighlightPainter.Mode.MARK_GIVEN));
      }
    }
    int startOffset = changeDelta.getStartTextIndex(pChangeSide);
    int endOffset = changeDelta.getEndTextIndex(pChangeSide);
    if (startOffset < endOffset)
      endOffset -= 1;
    pHighlightSpots.add(new _Highlight(new _HighlightSpot(startOffset, endOffset, changeDelta.getSecondaryDiffColor()), LineHighlightPainter.Mode.WHOLE_LINE));
  }

  /**
   * @param pChangeSide         EChangeSide for which the markings should be determined
   * @param linePartChangeDelta ILinePartChangeDelta for which to evaluate if it is drawn
   * @return true if the delta should be marked, false if the delta on the passed changeSide should not be drawn because it is the "empty" side of an ADD or DELETE
   */
  private static boolean _isMarkDelta(EChangeSide pChangeSide, ILinePartChangeDelta linePartChangeDelta)
  {
    return !((linePartChangeDelta.getChangeType() == EChangeType.ADD && pChangeSide == EChangeSide.OLD)
        || (linePartChangeDelta.getChangeType() == EChangeType.DELETE && pChangeSide == EChangeSide.NEW));
  }

  /**
   * applies the given textChangeEvent and the applies the given highlights
   *
   * @param pJEditorPane        JEditorPane that should be filled with text and colored
   * @param pHighlightsSupplier Supplier of list of _Highlight determining which additional areas get colored and the color of the areas
   *                            This is a supplier instead of a list because the text has to be inserted before the highlights are calculated
   */
  private static void _insertColoredText(JEditorPane pJEditorPane, IDeltaTextChangeEvent pTextChangeEvent, Supplier<List<_Highlight>> pHighlightsSupplier)
  {
    try
    {
      pTextChangeEvent.apply(pJEditorPane.getDocument());
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    _colorHighlights(pJEditorPane, pHighlightsSupplier.get());
  }

  /**
   * applies the given highlights to the editorPane
   *
   * @param pJEditorPane    EditorPane to add the highlights to
   * @param pHighlightSpots highlights to add to the editorPane
   */
  private static void _colorHighlights(JEditorPane pJEditorPane, List<_Highlight> pHighlightSpots)
  {
    LineHighlighter highlighter = new LineHighlighter();
    // TODO: check if highlighter is already a LineHighlighter and if so, call removeAllHighlights. Seems a better solution and could be better performance-wise
    pJEditorPane.setHighlighter(highlighter);
    try
    {
      for (_Highlight highlight : pHighlightSpots)
      {
        highlighter.addBackgroundHighlight(highlight.getStartIndex(), highlight.getEndOffset(), new LineHighlightPainter(highlight.getColor(), highlight.getMode()));
      }
    }
    catch (BadLocationException e)
    {
      // exception is okay, can happen if text is not set yet. Don't draw the highlight, should be drawn later on anyways
    }
  }

  /**
   * check if a only a thin line should be highlighted, instead of the whole line
   *
   * @param pChangeDelta Delta that the highlight is for
   * @param pChangeSide  pChangeSide which side of a IChangeDelta should be used
   * @return true if only a thin part of the line should be highlighted, false otherwise
   */
  private static boolean _isUseThinLine(IChangeDelta pChangeDelta, EChangeSide pChangeSide)
  {
    return pChangeDelta.getEndLine(pChangeSide) == pChangeDelta.getStartLine(pChangeSide);
  }

  /**
   * class to store the diverse spots that should be highlighted after
   * the IChangeDeltas are inserted.
   */
  private static class _Highlight
  {

    private final _HighlightSpot highlightSpot;
    private final LineHighlightPainter.Mode mode;

    _Highlight(_HighlightSpot pHighlightSpot, LineHighlightPainter.Mode pMode)
    {
      highlightSpot = pHighlightSpot;
      mode = pMode;
    }

    int getStartIndex()
    {
      return highlightSpot.getStartIndex();
    }

    int getEndOffset()
    {
      return highlightSpot.getEndIndex();
    }

    public LineHighlightPainter.Mode getMode()
    {
      return mode;
    }

    public Color getColor()
    {
      return highlightSpot.getColor();
    }
  }

  /**
   * Represents an area between two indices that should be highlighted
   */
  private static final class _HighlightSpot
  {
    private final int startIndex;
    private final int endIndex;
    private final Color color;

    private _HighlightSpot(int pStartIndex, int pEndIndex, Color pColor)
    {
      startIndex = pStartIndex;
      endIndex = pEndIndex;
      color = pColor;
    }

    public int getEndIndex()
    {
      return endIndex;
    }

    public int getStartIndex()
    {
      return startIndex;
    }

    public Color getColor()
    {
      return color;
    }
  }
}
