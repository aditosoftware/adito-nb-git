package de.adito.git.gui.swing;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.*;
import java.awt.*;
import java.util.Optional;
import java.util.logging.*;

/**
 * @author m.kaspera, 24.01.2020
 */
public class TextPaneUtil
{

  private static final Logger LOGGER = Logger.getLogger(TextPaneUtil.class.getName());

  /**
   * calculates the y coordinates of each line in the text editor
   *
   * @param pEditorPane JTextComponent
   * @param pView       View shows the textComponent
   * @return Array of LineNumbers, each representing one Line. The LineNumbers are sorted, so position 0 in the array is the LineNumber of line 1
   * @throws BadLocationException if one of the accessed lineNumbers is out of bounds
   */
  public static LineNumber[] calculateLineYPositions(@NonNull JTextComponent pEditorPane, @Nullable View pView) throws BadLocationException
  {
    int numLines = pEditorPane.getDocument().getDefaultRootElement().getElementCount();
    return _calculateLineNumbers(pEditorPane, pView, pEditorPane.getFontMetrics(pEditorPane.getFont()).getHeight(), 0,
                                 Math.max(0, numLines - 1), new LineNumber[numLines]);
  }

  /**
   * calculates the y coordinates of pStartIndex and pEndIndex lines and then checks if the lines in between have height of pLineHeight each.
   * If that is not the case, split the interval in 2 and check again
   * Finally return a Set with the coordinates for each line in the passed interval
   *
   * @param pEditorPane  JEditorPane containing the text for which theses lines are
   * @param pView        view mapping the model to viewSpace
   * @param pLineHeight  height of a line in the current font
   * @param pStartIndex  first line to check
   * @param pEndIndex    last line to check
   * @param pLineNumbers Array of LineNumbers, used as the return value (after input is filled into the array)
   * @return Set of LineNumbers
   * @throws BadLocationException if one of the accessed lineNumbers is out of bounds
   */
  private static LineNumber[] _calculateLineNumbers(@NonNull JTextComponent pEditorPane, @Nullable View pView,
                                                    int pLineHeight, int pStartIndex, int pEndIndex, LineNumber[] pLineNumbers)
      throws BadLocationException
  {
    LineNumber startNumber = _calculateLineNumberPos(pEditorPane, pView, pStartIndex);
    LineNumber endNumber = _calculateLineNumberPos(pEditorPane, pView, pEndIndex);
    if (startNumber == null || endNumber == null)
      return pLineNumbers;
    // we're down to two lines here, if the first one is the on taking up more space the for loop in the "else if" gives the space to the second line,
    // so seperate treatment here
    if (pEndIndex - pStartIndex <= 1)
    {
      pLineNumbers[pStartIndex] = new LineNumber(pStartIndex + 1, startNumber.getXCoordinate(), startNumber.getYCoordinate(), startNumber.getHeight());
      pLineNumbers[pEndIndex] = new LineNumber(pEndIndex + 1, endNumber.getXCoordinate(), endNumber.getYCoordinate(), endNumber.getHeight());
    }
    else if (endNumber.getYCoordinate() - startNumber.getYCoordinate() == (pEndIndex - pStartIndex) * pLineHeight)
    {
      for (int index = 0; index <= pEndIndex - pStartIndex; index++)
      {
        pLineNumbers[pStartIndex + index] = new LineNumber(pStartIndex + index + 1, startNumber.getXCoordinate(), startNumber.getYCoordinate() + index * pLineHeight, pLineHeight);
      }
    }
    else
    {
      _calculateLineNumbers(pEditorPane, pView, pLineHeight, pStartIndex, (pStartIndex + pEndIndex) / 2, pLineNumbers);
      _calculateLineNumbers(pEditorPane, pView, pLineHeight, (pStartIndex + pEndIndex) / 2, pEndIndex, pLineNumbers);
    }
    return pLineNumbers;
  }

  /**
   * @param pEditorPane JEditorPane containing the text for which theses lines are
   * @param pView       view mapping the model to viewSpace
   * @param pLineIndex  index of the line for which to calculate the position
   * @return LineNumber with the number of the line and its position
   * @throws BadLocationException if the lineIndex is out of bounds
   */
  @Nullable
  private static LineNumber _calculateLineNumberPos(@NonNull JTextComponent pEditorPane, @Nullable View pView, int pLineIndex) throws BadLocationException
  {
    if (pEditorPane.getDocument().getDefaultRootElement().getElementCount() < pLineIndex)
      return null;
    Element lineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(pLineIndex);
    if (lineElement == null)
      throw new BadLocationException("Element in Document for line was null", pLineIndex);
    int startOffset = lineElement.getStartOffset();
    if (pView != null)
    {
      try
      {
        return Optional.ofNullable(pView.modelToView(startOffset, Position.Bias.Forward, startOffset + 1, Position.Bias.Forward, new Rectangle()))
            .map(Shape::getBounds)
            .map(pRectangle -> new LineNumber(pLineIndex + 1, 0, (int) pRectangle.getY(), (int) pRectangle.getHeight()))
            .orElse(new LineNumber(pLineIndex + 1, 0, 0, 0));
      }
      // it is possible that the modelToView causes a mutex error if called outside the EDT (very small chance though). If we wait for the EDT here,
      // the result isn't properly rendered however -> quick way is to just catch the error. In the future, a rework may be done that properly uses the EDT thread
      catch (Error pError)
      {
        LOGGER.log(Level.WARNING, "Git Plugin: Error determining line position", pError);
      }
    }
    return null;

  }

}
