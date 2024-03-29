package de.adito.git.impl.observables;

import de.adito.util.reactive.AbstractListenerObservable;
import lombok.NonNull;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.Optional;

/**
 * Observable that observes a single property of an item and fires the new alue each time that specific property changes
 *
 * @author m.kaspera, 24.06.2019
 */
public class PropertyChangeObservable<RESULTTYPE> extends AbstractListenerObservable<PropertyChangeListener, JComponent, Optional<RESULTTYPE>>
{
  private final String propertyName;

  public PropertyChangeObservable(@NonNull JComponent pListenableValue, @NonNull String pPropertyName)
  {
    super(pListenableValue);
    propertyName = pPropertyName;
  }

  @NonNull
  @Override
  protected PropertyChangeListener registerListener(@NonNull JComponent pJComponent, @NonNull IFireable<Optional<RESULTTYPE>> pIFireable)
  {
    PropertyChangeListener listener = evt -> pIFireable.fireValueChanged(Optional.of((RESULTTYPE) evt.getNewValue()));
    pJComponent.addPropertyChangeListener(propertyName, listener);
    return listener;
  }

  @Override
  protected void removeListener(@NonNull JComponent pJComponent, @NonNull PropertyChangeListener pPropertyChangeListener)
  {
    pJComponent.removePropertyChangeListener(propertyName, pPropertyChangeListener);
  }
}
