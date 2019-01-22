package de.adito.git.gui.dialogs.panels.BaseDiffPanel.DiffPane;

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
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.kaspera, 21.01.2019
 */
public class LineNumbersColorModel implements IDiscardable
{

  // line height if an insert between two lines should be displayed
  private static final int INSERT_LINE_HEIGHT = 3;
  private final DiffPanelModel model;
  private final Insets editorPaneInsets;
  private final int modelNumber;
  private final Disposable disposable;
  private final List<ILineNumberColorsListener> listeners = new ArrayList<>();

  LineNumbersColorModel(@NotNull DiffPanelModel pModel, @NotNull JEditorPane pEditorPane, @NotNull Observable<Rectangle> pViewPortObs,
                        int pModelNumber)
  {

    model = pModel;
    editorPaneInsets = pEditorPane.getInsets();
    modelNumber = pModelNumber;
    Observable<FileChangesRectanglePair> pairObservable = Observable.combineLatest(pModel.getFileChangesObservable(), pViewPortObs,
                                                                                   FileChangesRectanglePair::new);
    disposable = pairObservable.subscribe((pPair -> _calculateLineNumColors(pEditorPane, pPair.getFileChangesEvent(), pPair.getRectangle())));
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    listeners.clear();
  }

  void addListener(ILineNumberColorsListener pListener)
  {
    listeners.add(pListener);
  }

  void removeListener(ILineNumberColorsListener pListener)
  {
    listeners.remove(pListener);
  }

  /**
   * @param pEditorPane       JEditorPane with the text from the IFileChangesEvent. It's UI defines the y values for the LineNumColors
   * @param pFileChangesEvent currentIFileChangesEvent
   * @param pViewRectangle    Rectangle with coordinates of the current viewPort
   */
  private void _calculateLineNumColors(JEditorPane pEditorPane, IFileChangesEvent pFileChangesEvent, Rectangle pViewRectangle)
  {
    List<LineNumberColor> lineNumberColors = new ArrayList<>();
    try
    {
      View view = pEditorPane.getUI().getRootView(pEditorPane);
      int lineCounter = 0;
      for (IFileChangeChunk fileChange : pFileChangesEvent.getNewValue())
      {
        int numLines = model.getGetEndLine().apply(fileChange) - model.getGetStartLine().apply(fileChange);
        if (fileChange.getChangeType() != EChangeType.SAME)
        {
          LineNumberColor lineNumberColor = _viewCoordinatesLineNumberColor(pEditorPane, lineCounter, numLines, fileChange, view, pViewRectangle);
          if (lineNumberColor != null)
          {
            lineNumberColors.add(lineNumberColor);
          }
        }
        lineCounter += numLines + model.getGetParityLines().apply(fileChange).length();
      }
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    _notifyListeners(lineNumberColors);
  }

  /**
   * @param pEditorPane    EditorPane that contains the text of the IFileChangeChunks in pFileChangesEvent
   * @param pLineCounter   actual number of the line, this is due to added parityLines
   * @param pNumLines      number of lines that this LineNumColor should encompass
   * @param pFileChange    IFileChangeChunk that is the reason for this LineNumColor
   * @param pView          rootView of the UI of the EditorPane, to determine the location of lines in view coordinates
   * @param pViewRectangle Rectangle with coordinates of the current viewPort
   * @return LineNumberColor with the gathered information about where and what color the LineNumberColor should be drawn, viewPortCoordinates
   * @throws BadLocationException i.e. if the line is out of bounds
   */
  private LineNumberColor _viewCoordinatesLineNumberColor(JEditorPane pEditorPane, int pLineCounter, int pNumLines, IFileChangeChunk pFileChange,
                                                          View pView, Rectangle pViewRectangle) throws BadLocationException
  {
    if (pLineCounter > pEditorPane.getDocument().getDefaultRootElement().getElementCount())
      return null;
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
      // adjust coordinates from view to viewPort coordinates
      bounds.y = bounds.y - pViewRectangle.y;
      bounds.x = bounds.x + editorPaneInsets.left;
      return new LineNumberColor(pFileChange.getChangeType().getDiffColor(), bounds);
    }
    throw new BadLocationException("could not find Element for provided lines", startingLineElement == null ? pLineCounter :
        pLineCounter + pNumLines + model.getGetParityLines().apply(pFileChange).length() - 1);
  }

  private void _notifyListeners(List<LineNumberColor> pNewValue)
  {
    for (ILineNumberColorsListener listener : listeners)
    {
      listener.lineNumberColorsChanged(modelNumber, pNewValue);
    }
  }

}
