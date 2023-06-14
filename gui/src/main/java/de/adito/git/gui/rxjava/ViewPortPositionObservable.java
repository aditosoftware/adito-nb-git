package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import lombok.NonNull;

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

  public ViewPortPositionObservable(@NonNull JViewport pListenableValue)
  {
    super(pListenableValue);
  }

  @NonNull
  @Override
  protected ChangeListener registerListener(@NonNull JViewport pJViewport, @NonNull IFireable<Rectangle> pFireable)
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
  protected void removeListener(@NonNull JViewport pJViewport, @NonNull ChangeListener pChangeListener)
  {
    pJViewport.removeChangeListener(pChangeListener);
  }
}
