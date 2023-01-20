package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.swing.LineNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.*;

/**
 * Keeps track of the y coordinates for the icons in a diff/merge panel
 *
 * @author m.kaspera, 13.12.2022
 */
public class IconInfoModel extends ListenableModel<IconInfoModelListener> implements IDiscardable, LineNumberListener
{

  static final int Y_ICON_OFFSET = 2;

  @NotNull
  private final LineNumberModel lineNumberModel;
  @NotNull
  private final JEditorPane editorPane;
  private final int acceptChangeIconXVal;
  private final int discardChangeIconXVal;
  @Nullable
  private final ImageIcon acceptIcon;
  @Nullable
  private final ImageIcon discardIcon;
  @NotNull
  private final EChangeSide changeSide;
  @NotNull
  private TreeMap<Integer, IconInfo> acceptCoordinateMapping = new TreeMap<>();
  @NotNull
  private TreeMap<Integer, IconInfo> discardCoordinateMapping = new TreeMap<>();


  /**
   * @param pLineNumberModel Model that keeps track of the line heights for the diffpane
   * @param pChangeSide      determines which side of IChangeDeltas is used for the IDeltaTextChangeEvents that are passed from the LineNumberModel
   * @param pEditorPane      EditorPane on whose text contents this model is based on (this model updates on changes to the text contents of the pane)
   * @param pAcceptIcon      icon used for the "accept changes" action. pass null if no accept action is wanted
   * @param pDiscardIcon     icon used for the "discard changes" action. pass null if no discard action is wanted
   * @param pOrientation     BorderLayout.EAST or BorderLayout.WEST, this parameter determines whether the discard or accept icon is the right or left icon
   */
  public IconInfoModel(@NotNull LineNumberModel pLineNumberModel, @NotNull EChangeSide pChangeSide, @NotNull JEditorPane pEditorPane, @Nullable ImageIcon pAcceptIcon,
                       @Nullable ImageIcon pDiscardIcon, @NotNull String pOrientation)
  {
    changeSide = pChangeSide;
    editorPane = pEditorPane;
    acceptIcon = pAcceptIcon;
    discardIcon = pDiscardIcon;
    lineNumberModel = pLineNumberModel;

    lineNumberModel.addListener(this);

    acceptChangeIconXVal = BorderLayout.WEST.equals(pOrientation) || pDiscardIcon == null ? 0 : pDiscardIcon.getIconWidth();
    if (BorderLayout.WEST.equals(pOrientation))
    {
      if (pAcceptIcon == null)
        discardChangeIconXVal = 16;
      else
        discardChangeIconXVal = pAcceptIcon.getIconWidth();
    }
    else
    {
      discardChangeIconXVal = 2;
    }
  }

  @Override
  public void discard()
  {
    lineNumberModel.removeListener(this);
    listenerList.clear();
  }

  @Override
  public void lineNumbersChanged(@NotNull IDeltaTextChangeEvent pTextChangeEvent, @NotNull LineNumber[] pLineNumbers)
  {
    calculateIconInfos(pTextChangeEvent, pLineNumbers);
  }

  /**
   * @param pYStart starting coordinate for the interval, must be smaller that pYEnd for this method to work correctly
   * @param pYEnd   end coordinate for the interval to be drawn, must be bigger or equal to pYStart for this method to work correctly
   * @return all icons that have to be drawn for the given interval. Also includes icons that only clip the interval. If pYStart is bigger than pYEnd returns an emtpy list
   */
  @NotNull
  public List<IconInfo> getIconInfosToDraw(int pYStart, int pYEnd)
  {
    if (pYStart > pYEnd)
      return List.of();

    // retain the reference to the current map for threadsafety. If discardCoordinateMapping changes, it gets a new reference -> we still retain the old reference in tmp
    TreeMap<Integer, IconInfo> tmp = discardCoordinateMapping;
    ArrayList<IconInfo> iconInfos = new ArrayList<>(tmp.subMap(pYStart, pYEnd).values());
    // it is possible that an IconInfo starts before the given y start coodinate, but still intersects the area that has to be drawn (due to its height).
    // We check if the last element before yStart is exactly such a case, and add it to the result if it is
    Optional.ofNullable(getClippingIcon(tmp, pYStart)).ifPresent(iconInfos::add);
    // also get the acceptIcons
    tmp = acceptCoordinateMapping;
    iconInfos.addAll(tmp.subMap(pYStart, pYEnd).values());
    Optional.ofNullable(getClippingIcon(tmp, pYStart)).ifPresent(iconInfos::add);
    return iconInfos;
  }

  /**
   * calculate the icons and their coordinates, based on the IChangeDeltas of the IDeltaTextChangeEvent and the height of the lineNumbers
   *
   * @param pTextChangeEvent IDeltaTextChangeEvent that contains all ChangeDeltas for the current diff/merge
   * @param pLineNumbers     array of LineNumbers that give the y coordinates of each line
   */
  private void calculateIconInfos(@NotNull IDeltaTextChangeEvent pTextChangeEvent, @NotNull LineNumber[] pLineNumbers)
  {
    List<IconInfo> acceptIconInfoList = new ArrayList<>();
    List<IconInfo> discardIconInfoList = new ArrayList<>();
    List<IChangeDelta> changeDeltas = pTextChangeEvent.getFileDiff() == null ? List.of() : pTextChangeEvent.getFileDiff().getChangeDeltas();
    for (IChangeDelta fileChange : changeDeltas)
    {
      // Chunks with type SAME have no buttons since contents are equal
      if (fileChange.getChangeStatus() == EChangeStatus.PENDING && editorPane.getDocument().getLength() > 0)
      {
        int yViewCoordinate = 0;
        int startLine = fileChange.getStartLine(changeSide);
        if (pLineNumbers.length > startLine)
          yViewCoordinate = pLineNumbers[startLine].getYCoordinate() + Y_ICON_OFFSET;

        if (acceptIcon != null)
          acceptIconInfoList.add(new IconInfo(acceptIcon, yViewCoordinate, acceptChangeIconXVal, fileChange));
        // discardIcon == null -> only accept button should be used (case DiffPanel)
        if (discardIcon != null)
        {
          discardIconInfoList.add(new IconInfo(discardIcon, yViewCoordinate, discardChangeIconXVal, fileChange));
        }
      }
    }
    acceptCoordinateMapping = calculateCoordinateMapping(acceptIconInfoList);
    discardCoordinateMapping = calculateCoordinateMapping(discardIconInfoList);
    // separate lists and treeMaps for accept and discard icons, since pairs of accept and discard icons have the same y value -> cannot be stored in the same treeMap
    notifyListeners();
  }

  /**
   * Retrieves the IconInfo that is associated with yStart, or if no such value is present, gets the IconInfo associated with the highest y value that is smaller than
   * yStart.
   * If the IconInfo starts outside the rectangle to be drawn but still sticks inside the area, the IconInfo is returned. Returns null in all other cases.
   * <pre>
   *
   * Examples (all adjacent "Icon" form a single Icon, ----- is the y coordinate of pYStart):
   *
   * Icon
   * Icon
   * Icon
   * ------------
   *
   * In this case, null is returned
   *
   *
   * Icon
   * Icon
   * Icon -------------
   *
   * In this case, the Icon is returned
   *
   *
   * Icon
   * Icon -------------
   * Icon
   *
   * In this case, the Icon is returned
   *
   *
   * Icon
   * Icon
   * Icon
   *
   * --------------
   * In this case, null is returned
   * </pre>
   *
   * @param pTreeMap Map to search for the clipping Icon
   * @param pYStart  Start of the area to draw (y value of the rectangle that represents the view)
   * @return IconInfo that is clipping the given y coordinate or null if no IconInfo is clipping the given y value
   */
  @Nullable
  private IconInfo getClippingIcon(@NotNull TreeMap<Integer, IconInfo> pTreeMap, int pYStart)
  {
    Map.Entry<Integer, IconInfo> lineNumberEntryBefore = pTreeMap.floorEntry(pYStart);
    if (Optional.ofNullable(lineNumberEntryBefore)
        .map(Map.Entry::getValue)
        .map(IconInfo::getIconCoordinates)
        .map(pRectangle -> pRectangle.y + pRectangle.height >= pYStart)
        .orElse(false))
    {
      return lineNumberEntryBefore.getValue();
    }
    return null;
  }

  /**
   * Create a treeMap from the given list of IconInfos. The key for the map is the y coordinate of the iconInfo
   *
   * @param pIconInfoList list of IconInfos
   * @return TreeMap of the IconsInfos, with their y value as the key and the IconInfo itself as value
   */
  @NotNull
  private TreeMap<Integer, IconInfo> calculateCoordinateMapping(@NotNull List<IconInfo> pIconInfoList)
  {
    TreeMap<Integer, IconInfo> iconInfoMap = new TreeMap<>();
    for (IconInfo iconInfo : pIconInfoList)
    {
      iconInfoMap.put(iconInfo.getIconCoordinates().y, iconInfo);
    }
    return iconInfoMap;
  }

  /**
   * Loops through the list of IconInfoModelListeners and informs them that the iconInfos changed
   */
  private void notifyListeners()
  {
    for (IconInfoModelListener listener : listenerList)
    {
      listener.iconInfosChanged();
    }
  }
}
