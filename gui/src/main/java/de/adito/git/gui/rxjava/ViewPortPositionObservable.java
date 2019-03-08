package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.Rectangle;

/**
 * "Mapping" from a listener to an Observable of the Rectangle of the JViewPort, Rectangle is the Rectangle that can be seen in the JScrollPane.
 * Coordinates of the Rectangle are in View coordinates
 *
 * @author m.kaspera, 07.03.2019
 */
public class ViewPortPositionObservable extends AbstractListenerObservable<ChangeListener, JViewport, Rectangle>
{

  public ViewPortPositionObservable(@NotNull JViewport pListenableValue)
  {
    super(pListenableValue);
  }

  @NotNull
  @Override
  protected ChangeListener registerListener(@NotNull JViewport pJViewport, @NotNull IFireable<Rectangle> pFireable)
  {
    ChangeListener changeListener = e -> {
      Rectangle rect = new Rectangle(pJViewport.getViewRect());
      rect.x = 0;
      pFireable.fireValueChanged(rect);
    };

    pJViewport.addChangeListener(changeListener);
    return changeListener;
  }

  @Override
  protected void removeListener(@NotNull JViewport pJViewport, @NotNull ChangeListener pChangeListener)
  {
    pJViewport.removeChangeListener(pChangeListener);
  }
}
