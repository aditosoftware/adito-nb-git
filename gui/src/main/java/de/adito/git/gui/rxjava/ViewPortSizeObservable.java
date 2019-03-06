package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;

/**
 * @author m.kaspera, 05.03.2019
 */
public class ViewPortSizeObservable extends AbstractListenerObservable<ChangeListener, JViewport, Dimension>
{

  private Dimension cachedViewportSize = new Dimension(0, 0);

  public ViewPortSizeObservable(@NotNull JViewport pListenableValue)
  {
    super(pListenableValue);
  }

  @NotNull
  @Override
  protected ChangeListener registerListener(@NotNull JViewport pJViewport, @NotNull AbstractListenerObservable.IFireable<Dimension> pIFireable)
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
  protected void removeListener(@NotNull JViewport pJViewport, @NotNull ChangeListener pChangeListener)
  {
    pJViewport.removeChangeListener(pChangeListener);
  }

}
