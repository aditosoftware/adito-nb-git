package de.adito.git.nbm.observables;

import de.adito.git.nbm.util.ProjectUtility;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
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

  private static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
  private static Observable<Optional<Project>> observableRef;

  private ActiveProjectObservable()
  {
    super(TopComponent.getRegistry());
  }

  @NotNull
  public static Observable<Optional<Project>> create()
  {
    if (observableRef == null)
    {
      observableRef = Observable.create(new ActiveProjectObservable())
          .startWith(_findProjectFromActives(TopComponent.getRegistry()))
          .distinctUntilChanged()
          .replay(1)
          .autoConnect(0, DISPOSABLES::add);
    }
    return observableRef;
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull TopComponent.Registry pListenableValue, @NotNull IFireable<Optional<Project>> pFireable)
  {
    PropertyChangeListener pcl = e -> pFireable.fireValueChanged(_findProjectFromActives(pListenableValue));
    pListenableValue.addPropertyChangeListener(pcl);
    return pcl;
  }

  /**
   * tries to find a project from any of the activated nodes or the activated topComponent.
   * Returns the first found Project or null if none can be found anywhere
   *
   * @param pTopComponentRegistry Netbeans TopComponentRegistry
   * @return currently selected project, wrapped in Optional in case no project can be found
   */
  @NotNull
  private static Optional<Project> _findProjectFromActives(TopComponent.Registry pTopComponentRegistry)
  {
    Node[] activatedNodes = pTopComponentRegistry.getActivatedNodes();
    if (activatedNodes != null)
    {
      for (Node node : activatedNodes)
      {
        Project currProject = ProjectUtility.findProject(node);
        if (currProject != null)
        {
          return Optional.of(currProject);
        }
      }
    }
    TopComponent activatedTopComponent = pTopComponentRegistry.getActivated();
    return Optional.ofNullable(activatedTopComponent == null ? null : activatedTopComponent.getLookup().lookup(Project.class));
  }

  @Override
  protected void removeListener(@NotNull TopComponent.Registry pListenableValue, @NotNull PropertyChangeListener pListener)
  {
    pListenableValue.removePropertyChangeListener(pListener);
  }

  public static void dispose()
  {
    ActiveProjectObservable.DISPOSABLES.clear();
  }
}
