package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane;

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
  private List<LineNumber> lineNumbers = new ArrayList<>();
  private List<LineNumberColor> lineNumberColors = new ArrayList<>();
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
    setBackground(new Color(0xff313335, true));
    model = pModel;
    disposable = Observable.combineLatest(
        pModel.getFileChangesObservable(), pDisplayedArea, FileChangesRectanglePair::new)
        .subscribe(
            pPair -> SwingUtilities.invokeLater(() -> {
              lineNumbers = _calculateLineNumbers(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle());
              lineNumberColors = _calculateLineNumberColors(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle());
              repaint();
            }));
  }

  @Override
  protected void paintComponent(Graphics pGraphics)
  {
    super.paintComponent(pGraphics);
    // passing the local variable here, this means we pass a reference to the current list. Combine this with always creating a new, separate list and
    // assigning it and there should be no ConcurrentModificationExceptions
    _paintLines(pGraphics, lineNumbers, lineNumberColors);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }


  /**
   * @param pEditorPane       EditorPane that contains the text of the IFileChangeChunks in pFileChangesEvent
   * @param pFileChangesEvent IFileChangesEvent that contains the most up-to-date IFileChangeChunks
   * @param pRectangle        Rectangle with the coordinates of the viewPort
   * @return List of LineNumberColors that should be drawn
   */
  private List<LineNumberColor> _calculateLineNumberColors(JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, Rectangle pRectangle)
  {
    ArrayList<LineNumberColor> lineNumColors = new ArrayList<>();
    try
    {
      View view = pEditorPane.getUI().getRootView(pEditorPane);
      int lineCounter = 0;
      for (IFileChangeChunk fileChange : pFileChangesEvent.getNewValue())
      {
        int numLines = model.getGetEndLine().apply(fileChange) - model.getGetStartLine().apply(fileChange);
        if (fileChange.getChangeType() != EChangeType.SAME)
        {
          LineNumberColor lineNumberColor = _addLineNumColor(pEditorPane, lineCounter, numLines, fileChange, view, pRectangle);
          if (lineNumberColor != null)
            lineNumColors.add(lineNumberColor);
        }
        lineCounter += numLines + model.getGetParityLines().apply(fileChange).length();
      }
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    return lineNumColors;
  }

  /**
   * @param pEditorPane  EditorPane that contains the text of the IFileChangeChunks in pFileChangesEvent
   * @param pLineCounter actual number of the line, this is due to added parityLines
   * @param pNumLines    number of lines that this LineNumColor should encompass
   * @param pFileChange  IFileChangeChunk that is the reason for this LineNumColor
   * @param pView        rootView of the UI of the EditorPane, to determine the location of lines in view coordinates
   * @param pRectangle   Rectangle with the coordinates of the viewPort
   * @return LineNumberColor with the gathered information about where and what color the LineNumberColor should be drawn
   * @throws BadLocationException i.e. if the line is out of bounds
   */
  private LineNumberColor _addLineNumColor(JEditorPane pEditorPane, int pLineCounter, int pNumLines,
                                           IFileChangeChunk pFileChange, View pView, Rectangle pRectangle) throws BadLocationException
  {
    Element startingLineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(pLineCounter);
    Element endingLineElement = pEditorPane.getDocument().getDefaultRootElement()
        .getElement(pLineCounter + pNumLines + model.getGetParityLines().apply(pFileChange).length() - 1);
    if (startingLineElement != null && endingLineElement != null)
    {
      Rectangle bounds;
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
                                   endingLineElement.getEndOffset(), Position.Bias.Backward, new Rectangle()).getBounds();
      }
      if (pRectangle.intersects(bounds))
      {
        bounds.y = bounds.y - pRectangle.y + editorPaneInsets.top;
        bounds.x = bounds.x + editorPaneInsets.left;
        return new LineNumberColor(pFileChange.getChangeType().getDiffColor(), bounds);
      }
    }
    return null;
  }

  /**
   * @param pEditorPane       JEditorPane that contains the text for which the line numbers should be drawn
   * @param pFileChangesEvent most recent IFileChangesEvent
   * @param pViewWindow       Coordinates of the viewPort window, in view coordinates
   */
  private List<LineNumber> _calculateLineNumbers(@NotNull JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, Rectangle pViewWindow)
  {
    ArrayList<LineNumber> lineNumberings = new ArrayList<>();
    try
    {
      View view = pEditorPane.getUI().getRootView(pEditorPane);
      int lineCounter = 0;
      int numberedLineCounter = 1;
      for (IFileChangeChunk fileChange : pFileChangesEvent.getNewValue())
      {
        int numLines = model.getGetEndLine().apply(fileChange) - model.getGetStartLine().apply(fileChange);
        for (int index = 0; index < numLines; index++)
        {
          Element lineElement = pEditorPane.getDocument().getDefaultRootElement().getElement(lineCounter + index);
          if (lineElement == null)
            break;
          int startOffset = lineElement.getStartOffset();
          int yViewCoordinate = view.modelToView(startOffset, Position.Bias.Forward, startOffset + 1, Position.Bias.Forward, new Rectangle())
              .getBounds().y;
          int yVisibleCoordinate = yViewCoordinate - pViewWindow.y;
          Rectangle numberBounds = new Rectangle(editorPaneInsets.left + 2, yViewCoordinate, Integer.MAX_VALUE, 16);
          if (pViewWindow.intersects(numberBounds))
            lineNumberings.add(new LineNumber(numberedLineCounter + index,
                                              yVisibleCoordinate, numberBounds.x));
        }
        numberedLineCounter += numLines;
        lineCounter += numLines + model.getGetParityLines().apply(fileChange).length();
      }
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    return lineNumberings;
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
    for (LineNumber lineNumber : pLineNumbers)
    {
      ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      pGraphics.setColor(new Color(0xff888888, true));
      pGraphics.drawString(lineNumber.getNumber(), lineNumber.getXCoordinate(),
                           lineNumber.getYCoordinate() + pGraphics.getFontMetrics().getHeight());
    }
  }
}
