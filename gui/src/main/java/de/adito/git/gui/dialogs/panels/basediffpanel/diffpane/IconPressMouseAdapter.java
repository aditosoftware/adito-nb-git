package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IChangeDelta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * MouseAdapter/Listener that checks if the mouse click occurred on any of the discard/accept Icons and, if that is the case, applies the
 * accept or discard change function on it.
 *
 * @author m.kaspera, 10.01.2019
 */
class IconPressMouseAdapter extends MouseAdapter
{

  private final int iconWidth;
  @Nullable
  private final Consumer<IChangeDelta> doOnDiscard;
  @Nullable
  private final Consumer<IChangeDelta> doOnAccept;
  @NotNull
  private final IconInfoModel iconInfoModel;
  @NotNull
  private final Supplier<Rectangle> viewArea;
  private final boolean isWestOrientation;

  IconPressMouseAdapter(int pIconWidth, @Nullable Consumer<IChangeDelta> pDoOnAccept, @Nullable Consumer<IChangeDelta> pDoOnDiscard,
                        @NotNull IconInfoModel pIconInfoModel, @NotNull Supplier<Rectangle> pViewArea, boolean pIsWestOrientation)
  {

    iconWidth = pIconWidth;
    doOnDiscard = pDoOnDiscard;
    doOnAccept = pDoOnAccept;
    iconInfoModel = pIconInfoModel;
    viewArea = pViewArea;
    isWestOrientation = pIsWestOrientation;
  }

  @Override
  public void mousePressed(MouseEvent pEvent)
  {
    if (pEvent.getButton() == MouseEvent.BUTTON1)
    {
      int viewYPos = viewArea.get().y;
      Point iconSpavePoint = new Point(pEvent.getPoint().x, pEvent.getPoint().y + viewYPos);
      // iterate over all drawn iconInfos and check if any of them are at the position of the mouseclick
      for (IconInfo iconInfo : iconInfoModel.getIconInfosToDraw(viewYPos, viewYPos + viewArea.get().height))
      {
        if (iconInfo.getIconCoordinates().contains(iconSpavePoint))
        {
          // check if the discard or accept button was pressed, done via x coordinate of the click
          if (doOnDiscard != null && ((pEvent.getX() > iconWidth && isWestOrientation) || (pEvent.getX() < iconWidth && !isWestOrientation)))
          {
            doOnDiscard.accept(iconInfo.getChangeDelta());
          }
          else if (doOnAccept != null)
          {
            doOnAccept.accept(iconInfo.getChangeDelta());
          }
          break;
        }
      }
    }
  }

}
