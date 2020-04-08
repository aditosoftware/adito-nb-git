package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.EChangeStatus;
import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanelModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Dimension;
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
  private final int modelNumber;
  private final Disposable disposable;
  private final Disposable areaDisposable;
  private final List<ILineNumberColorsListener> eagerListeners = new ArrayList<>();
  private final List<ILineNumberColorsListener> lazyListeners = new ArrayList<>();
  private List<LineNumberColor> viewCoordinatesColors = new ArrayList<>();

  LineNumbersColorModel(@NotNull DiffPanelModel pModel, @NotNull JEditorPane pEditorPane, @NotNull Observable<Rectangle> pViewPortObs,
                        @NotNull Observable<Dimension> pViewAreaObs, int pModelNumber)
  {

    model = pModel;
    modelNumber = pModelNumber;
    Observable<Rectangle> viewPortObservable = Observable.combineLatest(pModel.getFileChangesObservable(), pViewPortObs,
                                                                        (pChangesEvent, pViewPort) -> pViewPort);
    Observable<IDeltaTextChangeEvent> eventObservable = Observable.combineLatest(pModel.getFileChangesObservable(), pViewAreaObs,
                                                                                 (pChangesEvent, pArea) -> pChangesEvent);
    areaDisposable = eventObservable.subscribe(pEvent -> _calculateLineNumColors(pEditorPane, pEvent));
    disposable = viewPortObservable.subscribe(this::_calculateRelativeLineNumberColors);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    areaDisposable.dispose();
    eagerListeners.clear();
    lazyListeners.clear();
  }

  /**
   * Eager listeners are notified each time the viewPort moves, and the list they get is in viewPortCoordinates
   *
   * @param pListener Listener that wants to be notified
   */
  void addEagerListener(ILineNumberColorsListener pListener)
  {
    eagerListeners.add(pListener);
  }

  /**
   * Lazy Listeners are only notified when the area of the viewPort changes, and the list they get is in viewCoordinates
   *
   * @param pListener Listener that wants to be notified
   */
  void addLazyListener(ILineNumberColorsListener pListener)
  {
    lazyListeners.add(pListener);
  }

  /**
   * removes the listener from both listeners lists (eager and lazy). Also call this if the listener is only registered in one of the lists
   *
   * @param pListener Listener that wants to stop getting notifications
   */
  void removeListener(ILineNumberColorsListener pListener)
  {
    eagerListeners.remove(pListener);
    lazyListeners.remove(pListener);
  }

  /**
   * calculates the ViewPortCoordinates of the lineNumberColors for the eager listeners
   *
   * @param pViewPortRect Rectangle describing the current position of the viewPort
   */
  private void _calculateRelativeLineNumberColors(Rectangle pViewPortRect)
  {
    // store a reference to the list so that if the list of this object is exchanged (because of some change) we can continue iterating over the
    // "copy". This only works because the viewCoordinatesColors list is never changed, only re-assigned (which is why we need the pointer here)
    List<LineNumberColor> lineNumList = viewCoordinatesColors;
    List<LineNumberColor> viewPortCordList = new ArrayList<>();
    for (LineNumberColor lineNumberColor : lineNumList)
    {
      Rectangle coloredArea = new Rectangle(lineNumberColor.getColoredArea().x, lineNumberColor.getColoredArea().y - pViewPortRect.y,
                                            lineNumberColor.getColoredArea().width, lineNumberColor.getColoredArea().height);
      viewPortCordList.add(new LineNumberColor(lineNumberColor.getColor(), coloredArea));
    }
    _notifyEagerListeners(viewPortCordList);
  }

  /**
   * Calculates the view coordinates of the lineNumberColors and notifies lazy listeners of the new list
   *
   * @param pEditorPane       JEditorPane with the text from the IFileChangesEvent. It's UI defines the y values for the LineNumColors
   * @param pFileChangesEvent currentIFileChangesEvent
   */
  private void _calculateLineNumColors(JEditorPane pEditorPane, IDeltaTextChangeEvent pFileChangesEvent)
  {
    List<LineNumberColor> lineNumberColors = new ArrayList<>(pEditorPane.getDocument().getDefaultRootElement().getElementCount());
    try
    {
      View view = pEditorPane.getUI().getRootView(pEditorPane);
      List<IChangeDelta> changeDeltas = pFileChangesEvent.getFileDiff() == null ? List.of() : pFileChangesEvent.getFileDiff().getChangeDeltas();
      for (IChangeDelta fileChange : changeDeltas)
      {
        if (fileChange.getChangeStatus().getChangeStatus() == EChangeStatus.PENDING)
        {
          int numLines = fileChange.getEndLine(model.getChangeSide()) - fileChange.getStartLine(model.getChangeSide());
          if (fileChange.getStartLine(model.getChangeSide()) <= pEditorPane.getDocument().getDefaultRootElement().getElementCount())
          {
            LineNumberColor lineNumberColor = _viewCoordinatesLineNumberColor(pEditorPane, fileChange.getStartLine(model.getChangeSide()), numLines, fileChange, view);
            lineNumberColors.add(lineNumberColor);
          }
          else
          {
            SwingUtilities.invokeLater(() -> _calculateLineNumColors(pEditorPane, pFileChangesEvent));
            return;
          }
        }
      }
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
    viewCoordinatesColors = lineNumberColors;
    _notifyLazyListeners(lineNumberColors);
  }

  /**
   * @param pEditorPane  EditorPane that contains the text of the IFileChangeChunks in pFileChangesEvent
   * @param pLineCounter actual number of the line, this is due to added parityLines
   * @param pNumLines    number of lines that this LineNumColor should encompass
   * @param pFileChange  IFileChangeChunk that is the reason for this LineNumColor
   * @param pView        rootView of the UI of the EditorPane, to determine the location of lines in view coordinates
   * @return LineNumberColor with the gathered information about where and what color the LineNumberColor should be drawn, view coordinates
   * @throws BadLocationException i.e. if the line is out of bounds
   */
  private LineNumberColor _viewCoordinatesLineNumberColor(JEditorPane pEditorPane, int pLineCounter, int pNumLines, IChangeDelta pFileChange,
                                                          View pView) throws BadLocationException
  {
    Element startingLineElement = pEditorPane.getDocument().getDefaultRootElement()
        .getElement(Math.min(pEditorPane.getDocument().getDefaultRootElement().getElementCount() - 1, pLineCounter));
    Element endingLineElement = pEditorPane.getDocument().getDefaultRootElement()
        .getElement(Math.min(pEditorPane.getDocument().getDefaultRootElement().getElementCount() - 1, Math.max(0, pLineCounter + pNumLines - 1)));
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
      return new LineNumberColor(pFileChange.getChangeStatus().getChangeType().getDiffColor(), bounds);
    }
    throw new BadLocationException("could not find Element for provided lines", startingLineElement == null ? pLineCounter :
        pLineCounter + pNumLines - 1);
  }

  private void _notifyEagerListeners(List<LineNumberColor> pNewValue)
  {
    _notifyListeners(pNewValue, eagerListeners);
  }

  private void _notifyLazyListeners(List<LineNumberColor> pNewValue)
  {
    _notifyListeners(pNewValue, lazyListeners);
  }

  private void _notifyListeners(List<LineNumberColor> pNewValue, List<ILineNumberColorsListener> listeners)
  {
    for (ILineNumberColorsListener listener : listeners)
    {
      listener.lineNumberColorsChanged(modelNumber, pNewValue);
    }
  }

}
