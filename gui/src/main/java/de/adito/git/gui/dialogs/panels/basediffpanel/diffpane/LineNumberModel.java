package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import com.google.common.annotations.VisibleForTesting;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.gui.swing.TextPaneUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.View;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks the y coordinates for the lineNumbers in an editor, updated based on events in the FileChangesObservable of the DiffPanelModel (text changed -> number of
 * lines can change) and a resize of the editor window (possible line wrapping or zoom level changed -> positions of the lines can change)
 *
 * @author m.kaspera, 09.12.2022
 */
public class LineNumberModel extends ListenableModel<LineNumberListener> implements IDiscardable
{

  private static final Logger LOGGER = Logger.getLogger(LineNumberModel.class.getName());
  @NonNull
  private final Disposable areaDisposable;
  @NonNull
  private LineNumber[] lineNumberInfos = new LineNumber[0];
  @NonNull
  private TreeMap<Integer, LineNumber> coordinateMapping = new TreeMap<>();

  /**
   * @param pTextChangeEventObservable Observable of DeltaTextChangeEvents that fires each time the user accepts or discards changes or manually inputs text
   * @param pEditorPane                editorPane containing the text, used to determine the y coordinates of the lines
   * @param pViewAreaObs               Observable of the dimension of the viewPort, triggering on resize or zoom events
   */
  public LineNumberModel(@NonNull Observable<IDeltaTextChangeEvent> pTextChangeEventObservable, @NonNull JEditorPane pEditorPane,
                         @NonNull Observable<Dimension> pViewAreaObs)
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
   * @return Collection of all lineNumbers whose y coordinates lie in the given range, returns an empty list if pYStart is bigger than pYEnd
   */
  @NonNull
  public Collection<LineNumber> getLineNumbersToDraw(int pYStart, int pYEnd)
  {
    if (pYStart > pYEnd)
      return List.of();

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
  void calculateLineNumColors(@NonNull IDeltaTextChangeEvent pEvent, @NonNull JEditorPane pEditorPane)
  {
    SwingUtil.invokeInEDT(() -> {
      try
      {
        View view = pEditorPane.getUI().getRootView(pEditorPane);
        lineNumberInfos = TextPaneUtil.calculateLineYPositions(pEditorPane, view);
      }
      catch (BadLocationException pE)
      {
        // just log the exception and return the LineNumbers we could calculate. This way, the model may be able to fully calculate all LineNumbers on the next
        // event and is not broken
        LOGGER.log(Level.WARNING, pE, () -> "Git Plugin: Could not calculate LineNumber coordinates");
      }
      coordinateMapping = calculateCoordinateMapping(lineNumberInfos);
      notifyListeners(pEvent, lineNumberInfos);
    });
  }

  /**
   * Calculate a treeMap of the LineNumbers and their y coordinates for fast access of a given range of y values
   *
   * @param pViewCoordinatesColors Array of LineNumbers
   * @return TreeMap with the LineNumbers as values and the y coordinates of the LineNumbers as value
   */
  @NonNull
  private TreeMap<Integer, LineNumber> calculateCoordinateMapping(LineNumber @NonNull [] pViewCoordinatesColors)
  {
    TreeMap<Integer, LineNumber> lineNumberMap = new TreeMap<>();
    for (LineNumber lineNumber : pViewCoordinatesColors)
    {
      lineNumberMap.put(lineNumber.getYCoordinate(), lineNumber);
    }
    return lineNumberMap;
  }

  @Override
  public void discard()
  {
    areaDisposable.dispose();
    discardListeners();
  }

  /**
   * Loops through the list of LineNumberListeners and informs them that the LineNumbers changed
   *
   * @param pEvent    IDeltaTextChangeEvent that trigger the recalculation
   * @param pNewValue new/updated values for the LineNumbers
   */
  private void notifyListeners(@NonNull IDeltaTextChangeEvent pEvent, LineNumber @NonNull [] pNewValue)
  {
    for (LineNumberListener listener : listeners)
    {
      listener.lineNumbersChanged(pEvent, pNewValue);
    }
  }
}
