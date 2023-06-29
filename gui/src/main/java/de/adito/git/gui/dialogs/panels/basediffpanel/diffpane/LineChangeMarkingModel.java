package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import com.google.common.annotations.VisibleForTesting;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.swing.LineNumber;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.BadLocationException;
import java.awt.Rectangle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Model that keeps track of the colored areas that show changes in a diff/merge. These colored areas are based on the ChangeDeltas and the location of the lines
 * in the editor. This model does not factor in a potential scrollPane and thus the coordinates of the colored areas of this model should be considered absolute
 * coordinates
 *
 * @author m.kaspera, 09.12.2022
 */
public class LineChangeMarkingModel extends ListenableModel<LineNumberColorsListener> implements IDiscardable, LineNumberListener
{

  /**
   * line height if an insert between two lines should be displayed
   */
  @VisibleForTesting
  static final int INSERT_LINE_HEIGHT = 3;
  private static final Logger LOGGER = Logger.getLogger(LineNumberModel.class.getName());

  @NonNull
  private final LineNumberModel lineNumberModel;
  @NonNull
  private final EChangeSide changeSide;
  @NonNull
  private List<LineNumberColor> staticLineNumberColors = List.of();
  @NonNull
  private TreeMap<Integer, LineNumberColor> coordinateMapping = new TreeMap<>();

  /**
   * @param pLineNumberModel LineNumberModel that keeps track of the y coordinates of lines
   * @param pChangeSide      determines which side of IChangeDeltas is used for the IDeltaTextChangeEvents that are passed from the LineNumberModel
   */
  public LineChangeMarkingModel(@NonNull LineNumberModel pLineNumberModel, @NonNull EChangeSide pChangeSide)
  {
    changeSide = pChangeSide;
    lineNumberModel = pLineNumberModel;
    pLineNumberModel.addListener(this);
  }

  @Override
  public void discard()
  {
    lineNumberModel.removeListener(this);
    discardListeners();
  }

  /**
   * get all LineNumberColors of this model, {@code static} because this model does not factor in scrollPanes and the like
   *
   * @return List of all colored areas/LineNumberColors
   */
  @NonNull
  public List<LineNumberColor> getStaticLineNumberColors()
  {
    return staticLineNumberColors;
  }

  /**
   * Fetch a collection with all LineNumberColors that are visible in the given interval. Also includes any areas that only clip the interval
   *
   * @param pYStart starting coordinate for the interval, must be smaller that pYEnd
   * @param pYEnd   end coordinate for the interval to be drawn, must be bigger or equal to pYStart
   * @return Collection of LineNumberColors, empty list if pYStart is bigger than pYEnd
   */
  @NonNull
  public Collection<LineNumberColor> getLineNumberColorsToDraw(int pYStart, int pYEnd)
  {
    // subMap and floorEntry do not properly work if this is the case -> return emtpy list
    if (pYStart > pYEnd)
      return List.of();

    TreeMap<Integer, LineNumberColor> tmp = coordinateMapping;
    Collection<LineNumberColor> lineNumberColors = tmp.subMap(pYStart, pYEnd).values();
    // it is possible that a LineNumberColor starts before the given y start coodinate, but still intersects the area that has to be drawn (due to its height).
    // We check if the last element before yStart is exactly such a case, and add it to the result if it is
    @Nullable
    Map.Entry<Integer, LineNumberColor> lineNumberEntryBefore = tmp.floorEntry(pYStart);
    if (Optional.ofNullable(lineNumberEntryBefore)
        .map(Map.Entry::getValue)
        .map(LineNumberColor::getColoredArea)
        .map(pRectangle -> pRectangle.y + pRectangle.height >= pYStart)
        .orElse(false))
    {
      // create a new list here because the collection gotten from subMap.values does not support adding elements. Creating a new list is okay from a performance
      // standpoint because lineNumberColors should have a very limited number of entries
      lineNumberColors = new ArrayList<>(lineNumberColors);
      lineNumberColors.add(lineNumberEntryBefore.getValue());
    }
    return lineNumberColors;
  }

  /**
   * calculate the LineNumberColors after some change occurred
   *
   * @param pTextChangeEvent IDeltaTextChangeEvent that contains the ChangeDeltas who are the basis for the LineNumberColors
   * @param pLineNumbers     Array of LineNumbers that give the y coordinates for each line
   */
  private void calculateLineNumColors(@NonNull IDeltaTextChangeEvent pTextChangeEvent, LineNumber @NonNull [] pLineNumbers)
  {
    List<LineNumberColor> lineNumberColors = new ArrayList<>();
    try
    {
      List<IChangeDelta> changeDeltas = pTextChangeEvent.getFileDiff() == null ? List.of() : pTextChangeEvent.getFileDiff().getChangeDeltas();
      for (IChangeDelta fileChange : changeDeltas)
      {
        if (fileChange.getChangeStatus() != EChangeStatus.UNDEFINED)
        {
          int numLines = fileChange.getEndLine(changeSide) - fileChange.getStartLine(changeSide);
          if (fileChange.getStartLine(changeSide) <= pLineNumbers.length)
          {
            LineNumberColor lineNumberColor = viewCoordinatesLineNumberColor(fileChange.getStartLine(changeSide), numLines, fileChange, pLineNumbers);
            lineNumberColors.add(lineNumberColor);
          }
        }
      }
    }
    catch (BadLocationException pE)
    {
      // just log the exception and return those lineNumberColors we could calculate. This way, the model may be able to fully calculate all lineNumberColors on the next
      // event and is not broken
      LOGGER.log(Level.WARNING, pE, () -> "Git Plugin: Could not calculate LineNumberColors");
    }

    // re-assign the class variable to the new reference/list
    staticLineNumberColors = lineNumberColors;
    coordinateMapping = calculateCoordinateMapping(staticLineNumberColors);
    notifyListeners(lineNumberColors);
  }

  /**
   * Create a treeMap from the given list of LineNumberColors. The key for the map is the y coordinate of the LineNumberColor
   *
   * @param pViewCoordinatesColors list of LineNumberColors
   * @return TreeMap of the LineNumberColors, with their y value as the key and the LineNumberColor itself as value
   */
  @NonNull
  private TreeMap<Integer, LineNumberColor> calculateCoordinateMapping(@NonNull List<LineNumberColor> pViewCoordinatesColors)
  {
    TreeMap<Integer, LineNumberColor> lineNumberColorsMap = new TreeMap<>();
    for (LineNumberColor lineNumberColor : pViewCoordinatesColors)
    {
      lineNumberColorsMap.put(lineNumberColor.getColoredArea().y, lineNumberColor);
    }
    return lineNumberColorsMap;
  }

  /**
   * @param pLineCounter actual number of the line, this is due to added parityLines
   * @param pNumLines    number of lines that this LineNumColor should encompass
   * @param pFileChange  IFileChangeChunk that is the reason for this LineNumColor
   * @param pLineInfos   Array that contains the calculated coordinates of the lines
   * @return LineNumberColor with the gathered information about where and what color the LineNumberColor should be drawn, view coordinates
   * @throws BadLocationException i.e. if the line is out of bounds
   */
  private @NonNull LineNumberColor viewCoordinatesLineNumberColor(int pLineCounter, int pNumLines, @NonNull IChangeDelta pFileChange,
                                                                  @NonNull LineNumber[] pLineInfos) throws BadLocationException
  {
    LineNumber startingLineInfo = pLineInfos[Math.min(pLineInfos.length - 1, pLineCounter)];
    LineNumber endingLineInfo = pLineInfos[Math.min(pLineInfos.length - 1, Math.max(0, pLineCounter + pNumLines - 1))];
    Rectangle bounds;
    if (startingLineInfo != null && endingLineInfo != null)
    {
      // case "insert stuff here", no parity lines and pNumLines was 0 -> endingLineInfo is of line before startingLineInfo
      if (pNumLines == 0)
      {
        // to center the drawn line between two text lines, move up the top of the line INSERT_LINE_HEIGHT/2 pixels
        int yValue = startingLineInfo.getYCoordinate() - INSERT_LINE_HEIGHT / 2;
        // insert between the lines, so only color a few pixels between the lines
        bounds = new Rectangle(0, yValue, Integer.MAX_VALUE, INSERT_LINE_HEIGHT);
      }
      else
      {
        bounds = new Rectangle(0, startingLineInfo.getYCoordinate(), Integer.MAX_VALUE,
                               endingLineInfo.getYCoordinate() + endingLineInfo.getHeight() - startingLineInfo.getYCoordinate());
      }
      return new LineNumberColor(pFileChange.getDiffColor(), bounds);
    }
    throw new BadLocationException("could not find Element for provided lines", startingLineInfo == null ? pLineCounter :
        pLineCounter + pNumLines - 1);
  }

  /**
   * Loops through the list of IconInfoModelListeners and informs them that the LineNumberColors changed
   *
   * @param pLineNumberColors new list of LineNumbersColors the listeners should be informed about
   */
  private void notifyListeners(@NonNull List<LineNumberColor> pLineNumberColors)
  {
    for (LineNumberColorsListener listener : listeners)
    {
      listener.lineNumberColorsChanged(pLineNumberColors);
    }
  }

  @Override
  public void lineNumbersChanged(@NonNull IDeltaTextChangeEvent pTextChangeEvent, LineNumber @NonNull [] pLineNumbers)
  {
    calculateLineNumColors(pTextChangeEvent, pLineNumbers);
  }
}
