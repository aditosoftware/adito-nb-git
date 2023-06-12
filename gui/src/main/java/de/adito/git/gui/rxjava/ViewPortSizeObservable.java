package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;

/**
 * @author m.kaspera, 05.03.2019
 */
public class ViewPortSizeObservable extends AbstractListenerObservable<ChangeListener, JViewport, Dimension>
{

  private Dimension cachedViewportSize = new Dimension(0, 0);

  public ViewPortSizeObservable(@NonNull JViewport pListenableValue)
  {
    super(pListenableValue);
  }

  @NonNull
  @Override
  protected ChangeListener registerListener(@NonNull JViewport pJViewport, @NonNull AbstractListenerObservable.IFireable<Dimension> pIFireable)
  {
    ChangeListener changeListener = e -> {
      if (!pJViewport.getViewRect().getSize().equals(cachedViewportSize))
      {
        cachedViewportSize = pJViewport.getViewRect().getSize();
        pIFireable.fireValueChanged(pJViewport.getViewSize());
      }
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
