package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel that contains the line numbers of a given JTextPane. Arranges the numbers such that they fit the lines in the TextPane even if the font is
 * changed or there are virtual line breaks
 *
 * @author m.kaspera 13.12.2018
 */
class LineNumPanel extends JPanel implements IDiscardable
{

  private static final int INSERT_LINE_HEIGHT = 3;
  private static final int LINE_NUM_FACADE_WIDTH = 30;
  private final DiffPanelModel model;
  private final Disposable disposable;
  private Rectangle cachedViewRectangle = new Rectangle();
  // lists with Objects that contain information about what to draw. Never modify these lists by themselves, only re-assign them
  private List<LineNumber> lineNumbers = new ArrayList<>(); // all LineNumbers for the file in the editor, irrespective of if they are shown
  private List<LineNumber> drawnLineNumbers = new ArrayList<>(); // LineNumbers currently in the viewPort, these have to be drawn
  private List<LineNumberColor> lineNumberColors = new ArrayList<>(); // all LineNumberColors for the file in the editor
  private List<LineNumberColor> drawnLineNumberColors = new ArrayList<>(); // LineNumberColors currently in the viewPort, these have to be drawn
  private final Insets editorPaneInsets;


  /**
   * @param pModel         DiffPanelModel that contains some of the Functions that retrieve information, such as start/end line, of an FileChangeChunk
   * @param pEditorPane    JEditorPane that displays the text from the IFileChangeChunks in the pModel
   * @param pDisplayedArea Observable of the Rectangle of the viewPort
   */
  LineNumPanel(@NotNull DiffPanelModel pModel, JEditorPane pEditorPane, @NotNull Observable<Rectangle> pDisplayedArea)
  {
    editorPaneInsets = pEditorPane.getInsets();
    setPreferredSize(new Dimension(LINE_NUM_FACADE_WIDTH + editorPaneInsets.left + editorPaneInsets.right, 1));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    model = pModel;
    disposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pDisplayedArea, FileChangesRectanglePair::new)
        .subscribe(
            pPair -> SwingUtilities.invokeLater(() -> {
              _calculateLineNumbers(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle());
              _calculateLineNumColors(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle());
              repaint();
            }));
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    // passing the local variable here, this means we pass a reference to the current list. Combine this with always creating a new, separate list and
    // assigning it and there should be no ConcurrentModificationExceptions
    _paintLines(pGraphics, drawnLineNumbers, drawnLineNumberColors);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }

  /**
   * @param pEditorPane       JEditorPane that contains the text for which the line numbers should be drawn
   * @param pFileChangesEvent most recent IFileChangesEvent
   * @param pViewWindow       Coordinates of the viewPort window, in view coordinates
   */
  private void _calculateLineNumbers(@NotNull JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, Rectangle pViewWindow)
  {
    if (pViewWindow.width != cachedViewRectangle.width || lineNumbers.isEmpty() || pViewWindow.equals(cachedViewRectangle))
    {
      try
      {
        List<LineNumber> lineNums = new ArrayList<>();
        View view = pEditorPane.getUI().getRootView(pEditorPane);
        int lineCounter = 0;
        int numberedLineCounter = 1;
        for (IFileChangeChunk fileChange : pFileChangesEvent.getNewValue())
        {
          int numLines = model.getGetEndLine().apply(fileChange) - model.getGetStartLine().apply(fileChange);
          for (int index = 0; index < numLines; index++)
          {
            lineNums.add(_calculateLineNumberPosition(pEditorPane, numberedLineCounter, lineCounter, index, view));
          }
          numberedLineCounter += numLines;
          lineCounter += numLines + model.getGetParityLines().apply(fileChange).length();
        }
        lineNumbers = lineNums;
      }
      catch (BadLocationException pE)
      {
        throw new RuntimeException(pE);
      }
    }
    drawnLineNumbers = _calculateDrawnLineNumbers(lineNumbers, pViewWindow);
    cachedViewRectangle = pViewWindow;
  }

  /**
   * @param pLineNumbers list of all LineNumbers to be filtered
   * @param pViewWindow  Rectangle with the coordinates of the viewPort
   * @return list of LineNumbers of the the incoming list that are in the viewPort area
   */
  private List<LineNumber> _calculateDrawnLineNumbers(List<LineNumber> pLineNumbers, Rectangle pViewWindow)
  {
    List<LineNumber> lineNumbersToDraw = new ArrayList<>();
    for (LineNumber lineNumber : pLineNumbers)
    {
      pViewWindow.intersects(lineNumber.getXCoordinate(), lineNumber.getYCoordinate() - pViewWindow.y, Integer.MAX_VALUE, 16);
      lineNumbersToDraw.add(new LineNumber(Integer.valueOf(lineNumber.getNumber()), lineNumber.getYCoordinate() - pViewWindow.y,
                                           lineNumber.getXCoordinate()));
    }
    return lineNumbersToDraw;
  }

  /**
   * @param pEditorPane          JEditorPane containing the text for which theses lines are
   * @param pNumberedLineCounter number of actual lines before this changeChunk
   * @param pLineCounter         number of total lines (including parity lines) before this changeChunk
   * @param pIndex               current line inside the changeChunk
   * @param pView                View for getting the y coordinates for line numbers
   * @return LineNumber object for the line determined in the input parameters
   * @throws BadLocationException if the line is out of scope of the document
   */
  private LineNumber _calculateLineNumberPosition(JEditorPane pEditorPane, int pNumberedLineCounter,
                                                  int pLineCounter, int pIndex, View pView) throws BadLocationException
  {
    Element lineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(pLineCounter + pIndex);
    if (lineElement == null)
      throw new BadLocationException("Element in Document for line was null", pLineCounter + pIndex);
    int startOffset = lineElement.getStartOffset();
    int yViewCoordinate = pView.modelToView(startOffset, Position.Bias.Forward, startOffset + 1, Position.Bias.Forward, new Rectangle())
        .getBounds().y;
    return new LineNumber(pNumberedLineCounter + pIndex, yViewCoordinate, editorPaneInsets.left + 2);
  }

  /**
   * @param pEditorPane       JEditorPane with the text from the IFileChangesEvent. It's UI defines the y values for the LineNumColors
   * @param pFileChangesEvent currentIFileChangesEvent
   * @param pViewWindow       Rectangle with coordinates of the current viewPort
   */
  private void _calculateLineNumColors(JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, Rectangle pViewWindow)
  {
    if (pViewWindow.width != cachedViewRectangle.width || lineNumberColors.isEmpty() || pViewWindow.equals(cachedViewRectangle))
    {
      try
      {
        List<LineNumberColor> lineNumColors = new ArrayList<>();
        View view = pEditorPane.getUI().getRootView(pEditorPane);
        int lineCounter = 0;
        for (IFileChangeChunk fileChange : pFileChangesEvent.getNewValue())
        {
          int numLines = model.getGetEndLine().apply(fileChange) - model.getGetStartLine().apply(fileChange);
          if (fileChange.getChangeType() != EChangeType.SAME)
          {
            lineNumColors.add(_viewCoordinatesLineNumberColor(pEditorPane, lineCounter, numLines, fileChange, view));
          }
          lineCounter += numLines + model.getGetParityLines().apply(fileChange).length();
        }
        lineNumberColors = lineNumColors;
      }
      catch (BadLocationException pE)
      {
        throw new RuntimeException(pE);
      }
    }
    drawnLineNumberColors = _calculateDrawnLineNumberColors(lineNumberColors, pViewWindow);
    cachedViewRectangle = pViewWindow;
  }

  /**
   * @param pAllLineNumberColors list with all lineNumberColor objects, will be filtered by which are in the viewPort
   * @param pViewRectangle       Rectangle with coordinates of the current viewPort
   * @return list with all LineNumberColors that are in the viewPort and have to be drawn
   */
  private List<LineNumberColor> _calculateDrawnLineNumberColors(List<LineNumberColor> pAllLineNumberColors, Rectangle pViewRectangle)
  {
    List<LineNumberColor> lineNumberColorsToDraw = new ArrayList<>();
    for (LineNumberColor lineNumberColor : pAllLineNumberColors)
    {
      if (pViewRectangle.intersects(lineNumberColor.getColoredArea()))
      {
        Rectangle toDrawRect = new Rectangle(lineNumberColor.getColoredArea());
        toDrawRect.y = toDrawRect.y - pViewRectangle.y;
        toDrawRect.x = toDrawRect.x + editorPaneInsets.left;
        toDrawRect.width = LINE_NUM_FACADE_WIDTH;
        lineNumberColorsToDraw.add(new LineNumberColor(lineNumberColor.getColor(), toDrawRect));
      }
    }
    return lineNumberColorsToDraw;
  }

  /**
   * @param pEditorPane  EditorPane that contains the text of the IFileChangeChunks in pFileChangesEvent
   * @param pLineCounter actual number of the line, this is due to added parityLines
   * @param pNumLines    number of lines that this LineNumColor should encompass
   * @param pFileChange  IFileChangeChunk that is the reason for this LineNumColor
   * @param pView        rootView of the UI of the EditorPane, to determine the location of lines in view coordinates
   * @return LineNumberColor with the gathered information about where and what color the LineNumberColor should be drawn
   * @throws BadLocationException i.e. if the line is out of bounds
   */
  private LineNumberColor _viewCoordinatesLineNumberColor(JEditorPane pEditorPane, int pLineCounter, int pNumLines, IFileChangeChunk pFileChange,
                                                          View pView) throws BadLocationException
  {
    Element startingLineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(pLineCounter);
    Element endingLineElement = pEditorPane.getDocument().getDefaultRootElement()
        .getElement(pLineCounter + pNumLines + model.getGetParityLines().apply(pFileChange).length() - 1);
    Rectangle bounds;
    if (startingLineElement != null && endingLineElement != null)
    {
      // case "insert stuff here", no parity lines and pNumLines was 0 -> endingLineElement is of line before startingLineElement
      if (startingLineElement.getStartOffset() == endingLineElement.getEndOffset())
      {
        bounds = pView.modelToView(startingLineElement.getStartOffset(), new Rectangle(), Position.Bias.Forward).getBounds();
        bounds.width = getPreferredSize().width;
        // insert between the lines, so only color a few pixels between the lines
        bounds.height = INSERT_LINE_HEIGHT;
        // to center the drawn line between two text lines, move up the top of the line INSERT_LINE_HEIGHT/2 pixels
        bounds.y = bounds.y - INSERT_LINE_HEIGHT / 2;
      }
      else
      {
        bounds = pView.modelToView(startingLineElement.getStartOffset(), Position.Bias.Forward,
                                   endingLineElement.getEndOffset() - 1, Position.Bias.Backward, new Rectangle()).getBounds();
      }
      return new LineNumberColor(pFileChange.getChangeType().getDiffColor(), bounds);
    }
    throw new BadLocationException("could not find Element for provided lines", startingLineElement == null ? pLineCounter :
        pLineCounter + pNumLines + model.getGetParityLines().apply(pFileChange).length() - 1);
  }

  /**
   * Extracted to separate method to take advantage of the fact that java gives us a reference to the list we pass. If the list itself is never
   * changed and instead the list is only exchanged with another one (like above) this should mean that there are no concurrentModificationExceptions
   * and we do not need any locks or copied immutable lists
   *
   * @param pGraphics    Graphics object to paint with
   * @param pLineNumbers the list of line numbers to be drawn
   */
  private void _paintLines(Graphics pGraphics, List<LineNumber> pLineNumbers, List<LineNumberColor> pLineNumColors)
  {
    for (LineNumberColor lineNumberColor : pLineNumColors)
    {
      pGraphics.setColor(lineNumberColor.getColor());
      pGraphics.fillRect(lineNumberColor.getColoredArea().x, lineNumberColor.getColoredArea().y,
                         lineNumberColor.getColoredArea().width, lineNumberColor.getColoredArea().height);
    }
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    pGraphics.setColor(ColorPicker.DIFF_LINE_NUM);
    for (LineNumber lineNumber : pLineNumbers)
    {
      pGraphics.drawString(lineNumber.getNumber(), lineNumber.getXCoordinate(),
                           lineNumber.getYCoordinate() + pGraphics.getFontMetrics().getHeight());
    }
  }
}
