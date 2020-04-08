package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IChangeDelta;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * MouseAdapter/Listener that checks if the mouse click occurred on any of the discard/accept Icons and, if that is the case, applies the
 * accept or discard change function on it.
 * Also invokes a repaint() in a SwingUtilities.invokeLater runnable if any of the icons was the source of the mouseClick
 *
 * @author m.kaspera, 10.01.2019
 */
class IconPressMouseAdapter extends MouseAdapter
{

  private final int iconWidth;
  private final Consumer<IChangeDelta> doOnDiscard;
  private final Consumer<IChangeDelta> doOnAccept;
  private final Supplier<List<IconInfo>> iconInfoListSupplier;
  private final Supplier<Rectangle> viewArea;
  private final boolean isWestOrientation;

  IconPressMouseAdapter(int pIconWidth, Consumer<IChangeDelta> pDoOnAccept, Consumer<IChangeDelta> pDoOnDiscard,
                        Supplier<List<IconInfo>> pIconInfoListSupplier, Supplier<Rectangle> pViewArea, boolean pIsWestOrientation)
  {

    iconWidth = pIconWidth;
    doOnDiscard = pDoOnDiscard;
    doOnAccept = pDoOnAccept;
    iconInfoListSupplier = pIconInfoListSupplier;
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
      for (IconInfo iconInfo : iconInfoListSupplier.get())
      {
        if (iconInfo.getIconCoordinates().contains(iconSpavePoint))
        {
          // check if the discard or accept button was pressed, done via x coordinate of the click
          if (doOnDiscard != null && ((pEvent.getX() > iconWidth && isWestOrientation) || (pEvent.getX() < iconWidth && !isWestOrientation)))
          {
            doOnDiscard.accept(iconInfo.getChangeDelta());
          }
          else
          {
            doOnAccept.accept(iconInfo.getChangeDelta());
          }
          break;
        }
      }
    }
  }

}
