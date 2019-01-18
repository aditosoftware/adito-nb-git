package de.adito.git.gui.dialogs.panels.BaseDiffPanel.TextPanes.DiffPane;

import de.adito.git.api.data.IFileChangeChunk;

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
public class IconPressMouseAdapter extends MouseAdapter
{

  private final int iconWidth;
  private final Consumer<IFileChangeChunk> doOnDiscard;
  private final Consumer<IFileChangeChunk> doOnAccept;
  private final Supplier<List<IconInfo>> iconInfoListSupplier;
  private final boolean isWestOrientation;

  IconPressMouseAdapter(int pIconWidth, Consumer<IFileChangeChunk> pDoOnAccept, Consumer<IFileChangeChunk> pDoOnDiscard,
                        Supplier<List<IconInfo>> pIconInfoListSupplier, boolean pIsWestOrientation)
  {

    iconWidth = pIconWidth;
    doOnDiscard = pDoOnDiscard;
    doOnAccept = pDoOnAccept;
    iconInfoListSupplier = pIconInfoListSupplier;
    isWestOrientation = pIsWestOrientation;
  }

  @Override
  public void mousePressed(MouseEvent pEvent)
  {
    if (pEvent.getButton() == MouseEvent.BUTTON1)
    {
      for (IconInfo iconInfo : iconInfoListSupplier.get())
      {
        if (iconInfo.getIconCoordinates().contains(pEvent.getPoint()))
        {
          // check if the discard or accept button was pressed, done via x coordinate of the click
          if (doOnDiscard != null && ((pEvent.getX() > iconWidth && isWestOrientation) || (pEvent.getX() < iconWidth && !isWestOrientation)))
          {
            doOnDiscard.accept(iconInfo.getFileChangeChunk());
          }
          else
          {
            doOnAccept.accept(iconInfo.getFileChangeChunk());
          }
          break;
        }
      }
    }
  }

}
