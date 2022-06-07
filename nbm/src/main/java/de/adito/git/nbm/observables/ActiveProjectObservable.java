package de.adito.git.nbm.observables;

import de.adito.git.nbm.util.ProjectUtility;
import de.adito.util.reactive.AbstractListenerObservable;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeListener;
import java.util.Optional;

/**
 * Observable of the project found in the active nodes
 *
 * @author w.glanzer, 14.12.2018
 */
public class ActiveProjectObservable extends AbstractListenerObservable<PropertyChangeListener, TopComponent.Registry, Optional<Project>>
{

  private static final ObservableCache _CACHE = new ObservableCache();

  private ActiveProjectObservable()
  {
    super(TopComponent.getRegistry());
  }

  @NotNull
  public static Observable<Optional<Project>> create()
  {
    return _CACHE.calculateParallel("create", () -> Observable
        .combineLatest(Observable.create(new ActiveProjectObservable())
                           .distinctUntilChanged()
                           .filter(Optional::isPresent)
                           .startWithItem(ProjectUtility.findProjectFromActives(TopComponent.getRegistry())),
                       OpenProjectsObservable.create(),
                       (pOptionalProject, pProjects) -> pOptionalProject.map(pProject -> pProjects.contains(pProject) ? pProject : null)));
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull TopComponent.Registry pListenableValue, @NotNull IFireable<Optional<Project>> pFireable)
  {
    PropertyChangeListener pcl = e -> pFireable.fireValueChanged(ProjectUtility.findProjectFromActives(pListenableValue));
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
    new ObservableCacheDisposable(_CACHE).dispose();
  }
}
