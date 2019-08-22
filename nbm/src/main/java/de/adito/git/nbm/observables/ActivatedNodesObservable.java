package de.adito.git.nbm.observables;

import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeListener;

/**
 * Observable which fires, if the activated nodes changed
 *
 * @author w.glanzer, 14.12.2018
 */
public class ActivatedNodesObservable extends AbstractListenerObservable<PropertyChangeListener, TopComponent.Registry, Node[]>
{

  private static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
  private static Observable<Node[]> observableRef;

  private ActivatedNodesObservable()
  {
    super(TopComponent.getRegistry());
  }

  @NotNull
  public static Observable<Node[]> create()
  {
    if (observableRef == null)
    {
      observableRef = Observable.create(new ActivatedNodesObservable())
          .startWith(TopComponent.getRegistry().getActivatedNodes())
          .distinctUntilChanged()
          .replay(1)
          .autoConnect(0, DISPOSABLES::add);
    }
    return observableRef;
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull TopComponent.Registry pListenableValue, @NotNull IFireable<Node[]> pFireable)
  {
    PropertyChangeListener pcl = e -> pFireable.fireValueChanged(pListenableValue.getActivatedNodes());
    pListenableValue.addPropertyChangeListener(pcl);
    return pcl;
  }

  @Override
  protected void removeListener(@NotNull TopComponent.Registry pListenableValue, @NotNull PropertyChangeListener pListener)
  {
    pListenableValue.removePropertyChangeListener(pListener);
  }

  public static void dispose()
  {
    ActivatedNodesObservable.DISPOSABLES.clear();
  }
}
