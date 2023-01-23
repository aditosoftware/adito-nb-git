package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.IDiscardable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Model that keeps track of the height of LineNumberColors in reference to a viewport - meaning the coordinates are adjusted each time the viewport changes its position,
 * not just if the LineNumberColors itself are changed
 *
 * @author m.kaspera, 09.12.2022
 */
public class ViewLineChangeMarkingModel extends ListenableModel<ViewLineChangeMarkingsListener> implements IDiscardable, ChangeListener, LineNumberColorsListener
{

  @NotNull
  private final LineChangeMarkingModel lineChangeMarkingModel;
  @NotNull
  private final JViewport viewport;
  @NotNull
  private List<LineNumberColor> lineNumberColors = List.of();

  /**
   * @param pLineChangeMarkingModel LineChangeMarkingModel with the absolute positions of the LineNumberColors
   * @param pViewport               ViewPort whose view we track
   */
  public ViewLineChangeMarkingModel(@NotNull LineChangeMarkingModel pLineChangeMarkingModel, @NotNull JViewport pViewport)
  {
    lineChangeMarkingModel = pLineChangeMarkingModel;
    lineChangeMarkingModel.addListener(this);

    viewport = pViewport;
    viewport.addChangeListener(this);
  }

  @Override
  public void discard()
  {
    lineChangeMarkingModel.removeListener(this);
    viewport.removeChangeListener(this);
    discardListeners();
  }

  /**
   * @return List of all LineNumberColors this model tracks, with their position based on the viewports position
   */
  @NotNull
  public List<LineNumberColor> getLineNumberColors()
  {
    return lineNumberColors;
  }

  /**
   * calculates the ViewPortCoordinates of the lineNumberColors for the eager listeners
   *
   * @param pViewPortRect Rectangle describing the current position of the viewPort
   */
  private void calculateRelativeLineNumberColors(@NotNull List<LineNumberColor> pStaticLineNumberColors, @NotNull Rectangle pViewPortRect)
  {
    // store a reference to the list so that if the list of this object is exchanged (because of some change) we can continue iterating over the
    // "copy". This only works because the viewCoordinatesColors list is never changed, only re-assigned (which is why we need the pointer here)
    List<LineNumberColor> viewPortCordList = new ArrayList<>();
    for (LineNumberColor lineNumberColor : pStaticLineNumberColors)
    {
      Rectangle coloredArea = new Rectangle(lineNumberColor.getColoredArea().x, lineNumberColor.getColoredArea().y - pViewPortRect.y,
                                            lineNumberColor.getColoredArea().width, lineNumberColor.getColoredArea().height);
      viewPortCordList.add(new LineNumberColor(lineNumberColor.getColor(), coloredArea));
    }
    lineNumberColors = viewPortCordList;
    notifyListeners(viewPortCordList);
  }

  @Override
  public void stateChanged(ChangeEvent e)
  {
    calculateRelativeLineNumberColors(lineChangeMarkingModel.getStaticLineNumberColors(), viewport.getViewRect());
  }

  @Override
  public void lineNumberColorsChanged(@NotNull List<LineNumberColor> pNewValue)
  {
    calculateRelativeLineNumberColors(pNewValue, viewport.getViewRect());
  }

  /**
   * notify the listeners that the list of LineNumberColors that this model keeps has changed
   *
   * @param pLineNumberColors new list of LineNumberColors
   */
  private void notifyListeners(@NotNull List<LineNumberColor> pLineNumberColors)
  {
    for (ViewLineChangeMarkingsListener listener : listeners)
    {
      listener.viewLineChangeMarkingChanged(pLineNumberColors);
    }
  }
}
