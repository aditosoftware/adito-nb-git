package de.adito.git.impl.observables;

import de.adito.util.reactive.AbstractListenerObservable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.Optional;

/**
 * Observable that observes a single property of an item and fires the new alue each time that specific property changes
 *
 * @author m.kaspera, 24.06.2019
 */
public class PropertyChangeObservable extends AbstractListenerObservable<PropertyChangeListener, JComponent, Optional<Object>>
{
  private final String propertyName;

  public PropertyChangeObservable(@NotNull JComponent pListenableValue, @NotNull String pPropertyName)
  {
    super(pListenableValue);
    propertyName = pPropertyName;
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull JComponent pJComponent, @NotNull IFireable<Optional<Object>> pIFireable)
  {
    PropertyChangeListener listener = evt -> pIFireable.fireValueChanged(Optional.of(evt.getNewValue()));
    pJComponent.addPropertyChangeListener(propertyName, listener);
    return listener;
  }

  @Override
  protected void removeListener(@NotNull JComponent pJComponent, @NotNull PropertyChangeListener pPropertyChangeListener)
  {
    pJComponent.removePropertyChangeListener(propertyName, pPropertyChangeListener);
  }
}
