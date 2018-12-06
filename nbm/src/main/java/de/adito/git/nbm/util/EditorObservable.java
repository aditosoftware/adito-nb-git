package de.adito.git.nbm.util;

import de.adito.util.reactive.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.windows.*;

import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * @author a.arnold, 05.11.2018
 */
@NotNull
public class EditorObservable extends AbstractListenerObservable<PropertyChangeListener, TopComponent.Registry, Optional<TopComponent>>
{
  private EditorObservable()
  {
    super(TopComponent.getRegistry());
  }

  public static Observable<Optional<TopComponent>> create()
  {
    return Observables.create(new EditorObservable(), EditorObservable::_getCurrentEditor);
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull TopComponent.Registry pRegistry, @NotNull IFireable<Optional<TopComponent>> pFireable)
  {
    PropertyChangeListener listener = evt -> pFireable.fireValueChanged(_getCurrentEditor());
    pRegistry.addPropertyChangeListener(listener);
    return listener;
  }

  @Override
  protected void removeListener(@NotNull TopComponent.Registry pRegistry, @NotNull PropertyChangeListener pPropertyChangeListener)
  {
    pRegistry.removePropertyChangeListener(pPropertyChangeListener);
  }

  @NotNull
  private static Optional<TopComponent> _getCurrentEditor()
  {
    Set<? extends Mode> modes = WindowManager.getDefault().getModes();
    for (Mode mode : modes)
    {
      if ("editor".equals(mode.getName()))
      {
        return Optional.ofNullable(mode.getSelectedTopComponent());
      }
    }
    return Optional.empty();
  }
}
