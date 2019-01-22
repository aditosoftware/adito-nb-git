package de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPane;

import de.adito.git.api.ColorPicker;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
class LineNumPanel extends JPanel implements IDiscardable, ILineNumberColorsListener
{

  private final int lineNumFacadeWidth;
  private final DiffPanelModel model;
  private final Disposable disposable;
  private final Insets panelInsets = new Insets(2, 5, 2, 5);
  private final Insets editorInsets;
  private Rectangle cachedViewRectangle = new Rectangle();
  // lists with Objects that contain information about what to draw. Never modify these lists by themselves, only re-assign them
  private List<LineNumber> lineNumbers = new ArrayList<>(); // all LineNumbers for the file in the editor, irrespective of if they are shown
  private List<LineNumber> drawnLineNumbers = new ArrayList<>(); // LineNumbers currently in the viewPort, these have to be drawn
  private List<LineNumberColor> lineNumberColors = new ArrayList<>(); // all LineNumberColors for the file in the editor
  private final LineNumbersColorModel lineNumbersColorModel;


  /**
   * @param pModel         DiffPanelModel that contains some of the Functions that retrieve information, such as start/end line, of an FileChangeChunk
   * @param pEditorPane    JEditorPane that displays the text from the IFileChangeChunks in the pModel
   * @param pDisplayedArea Observable of the Rectangle of the viewPort
   */
  LineNumPanel(@NotNull DiffPanelModel pModel, JEditorPane pEditorPane, @NotNull Observable<Rectangle> pDisplayedArea,
               @NotNull LineNumbersColorModel pLineNumbersColorModel)
  {
    lineNumbersColorModel = pLineNumbersColorModel;
    editorInsets = pEditorPane.getInsets();
    lineNumFacadeWidth = getFontMetrics(getFont()).stringWidth(String.valueOf(_getLastLineNumber(pModel)));
    setBorder(new EmptyBorder(panelInsets));
    setPreferredSize(new Dimension(lineNumFacadeWidth + panelInsets.left + panelInsets.right, 1));
    setBackground(ColorPicker.DIFF_BACKGROUND);
    model = pModel;
    pLineNumbersColorModel.addListener(this);
    disposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pDisplayedArea, FileChangesRectanglePair::new)
        .subscribe(
            pPair -> SwingUtilities.invokeLater(() -> {
              _calculateLineNumbers(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle());
              repaint();
            }));
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    // passing the local variable here, this means we pass a reference to the current list. Combine this with always creating a new, separate list and
    // assigning it and there should be no ConcurrentModificationExceptions
    _paintLines(pGraphics, drawnLineNumbers, lineNumberColors);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    lineNumbersColorModel.removeListener(this);
    lineNumbersColorModel.discard();
  }

  @Override
  public void lineNumberColorsChanged(int pModelNumber, List<LineNumberColor> pNewValue)
  {
    lineNumberColors = pNewValue;
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
      if (pViewWindow.intersects(lineNumber.getXCoordinate(), lineNumber.getYCoordinate(), Integer.MAX_VALUE, 16))
      {
        lineNumbersToDraw.add(new LineNumber(Integer.valueOf(lineNumber.getNumber()),
                                             lineNumber.getYCoordinate() - pViewWindow.y - panelInsets.top - editorInsets.top,
                                             lineNumber.getXCoordinate()));
      }
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
    return new LineNumber(pNumberedLineCounter + pIndex, yViewCoordinate, panelInsets.left);
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
      if (lineNumberColor.getColoredArea().intersects(0, 0, lineNumFacadeWidth, cachedViewRectangle.height))
      {
        pGraphics.setColor(lineNumberColor.getColor());
        pGraphics.fillRect(lineNumberColor.getColoredArea().x, lineNumberColor.getColoredArea().y,
                           getWidth(), lineNumberColor.getColoredArea().height);
      }
    }
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    pGraphics.setColor(ColorPicker.DIFF_LINE_NUM);
    for (LineNumber lineNumber : pLineNumbers)
    {
      pGraphics.drawString(lineNumber.getNumber(), lineNumber.getXCoordinate(),
                           lineNumber.getYCoordinate() + pGraphics.getFontMetrics().getHeight());
    }
  }

  private int _getLastLineNumber(DiffPanelModel pModel)
  {
    List<IFileChangeChunk> changeChunks = pModel.getFileChangesObservable().blockingFirst().getNewValue();
    return pModel.getGetEndLine().apply(changeChunks.get(changeChunks.size() - 1));
  }
}
