package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import com.google.common.annotations.VisibleForTesting;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.gui.swing.TextPaneUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Tracks the y coordinates for the lineNumbers in an editor, updated based on events in the FileChangesObservable of the DiffPanelModel (text changed -> number of
 * lines can change) and a resize of the editor window (possible line wrapping or zoom level changed -> positions of the lines can change)
 *
 * @author m.kaspera, 09.12.2022
 */
public class LineNumberModel implements IDiscardable
{

  @NotNull
  private final Disposable areaDisposable;
  @NotNull
  private final List<LineNumberListener> listeners = new ArrayList<>();
  @NotNull
  private LineNumber[] lineNumberInfos = new LineNumber[0];
  @NotNull
  private TreeMap<Integer, LineNumber> coordinateMapping = new TreeMap<>();

  /**
   * @param pTextChangeEventObservable Observable of DeltaTextChangeEvents that fires each time the user accepts or discards changes or manually inputs text
   * @param pEditorPane                editorPane containing the text, used to determine the y coordinates of the lines
   * @param pViewAreaObs               Observable of the dimension of the viewPort, triggering on resize or zoom events
   */
  public LineNumberModel(@NotNull Observable<IDeltaTextChangeEvent> pTextChangeEventObservable, @NotNull JEditorPane pEditorPane,
                         @NotNull Observable<Dimension> pViewAreaObs)
  {
    areaDisposable = Observable.combineLatest(pTextChangeEventObservable, pViewAreaObs, (pChangesEvent, pArea) -> pChangesEvent)
        .throttleLatest(200, TimeUnit.MILLISECONDS, true)
        .subscribe(pEvent -> calculateLineNumColors(pEvent, pEditorPane));
  }

  /**
   * retrieve all LineNumbers whose y coordinates lie in a certain range
   *
   * @param pYStart start (smaller) y coordinate
   * @param pYEnd   end (bigger) y coordinate for which lineNumbers should be retrieved
   * @return Collection of all lineNumbers whose y coordinates lie in the given range
   */
  @NotNull
  public Collection<LineNumber> getLineNumbersToDraw(int pYStart, int pYEnd)
  {
    // assign coordinateMapping to a temp variable in case it would be assigned a different value during the subMap call -> ThreadSafety
    TreeMap<Integer, LineNumber> tmp = coordinateMapping;
    Collection<LineNumber> lineNumbers = tmp.subMap(pYStart, pYEnd).values();
    // it is possible that a LineNumberColor starts before the given y start coodinate, but still intersects the area that has to be drawn (due to its height).
    // We check if the last element before yStart is exactly such a case, and add it to the result if it is
    @Nullable
    Map.Entry<Integer, LineNumber> lineNumberEntryBefore = tmp.floorEntry(pYStart);
    if (Optional.ofNullable(lineNumberEntryBefore)
        .map(Map.Entry::getValue)
        .map(pLineNumber -> pLineNumber.getYCoordinate() + pLineNumber.getHeight() >= pYStart)
        .orElse(false) && !lineNumbers.contains(lineNumberEntryBefore.getValue()))
    {
      // create a new list here because the collection gotten from subMap.values does not support adding elements. Creating a new list is okay from a performance
      // standpoint because lineNumbers should have a very limited number of entries
      lineNumbers = new ArrayList<>(lineNumbers);
      lineNumbers.add(lineNumberEntryBefore.getValue());
    }
    return lineNumbers;
  }

  /**
   * Calculates the view coordinates of the lineNumbers and notifies listeners of the new list
   *
   * @param pEvent      IDeltaTextChangeEvent that triggered the new calculation of the y values
   * @param pEditorPane JEditorPane with the text from the IFileChangesEvent. It's UI defines the y values for the lines
   */
  @VisibleForTesting
  void calculateLineNumColors(@NotNull IDeltaTextChangeEvent pEvent, @NotNull JEditorPane pEditorPane)
  {
    SwingUtil.invokeInEDT(() -> {
      try
      {
        View view = pEditorPane.getUI().getRootView(pEditorPane);
        lineNumberInfos = TextPaneUtil.calculateLineYPositions(pEditorPane, view);
      }
      catch (Exception pE)
      {
        throw new RuntimeException(pE);
      }
      coordinateMapping = calculateCoordinateMapping(lineNumberInfos);
      _notifyListeners(pEvent, lineNumberInfos);
    });
  }

  /**
   * Calculatea treeMap of the LineNumbers and their y coordinates for fast access of a given range of y values
   *
   * @param pViewCoordinatesColors Array of LineNumbers
   * @return TreeMap with the LineNumbers as values and the y coordinates of the LineNumbers as value
   */
  @NotNull
  private TreeMap<Integer, LineNumber> calculateCoordinateMapping(@NotNull LineNumber[] pViewCoordinatesColors)
  {
    TreeMap<Integer, LineNumber> map = new TreeMap<>();
    for (LineNumber lineNumber : pViewCoordinatesColors)
    {
      map.put(lineNumber.getYCoordinate(), lineNumber);
    }
    return map;
  }

  @Override
  public void discard()
  {
    areaDisposable.dispose();
    listeners.clear();
  }

  /**
   * Add a listener that is notified if the number of lines or the coordinates of one or more lines are changed
   *
   * @param pListener Listener that wants to be notified
   */
  void addLineNumberListener(@NotNull LineNumberListener pListener)
  {
    listeners.add(pListener);
  }


  /**
   * removes the listener from the list of listeners to notify
   *
   * @param pListener Listener that wants to stop getting notifications
   */
  void removeLineNumberListener(@NotNull LineNumberListener pListener)
  {
    listeners.remove(pListener);
  }

  private void _notifyListeners(@NotNull IDeltaTextChangeEvent pEvent, @NotNull LineNumber[] pNewValue)
  {
    for (LineNumberListener listener : listeners)
    {
      listener.lineNumbersChanged(pEvent, pNewValue);
    }
  }
}
