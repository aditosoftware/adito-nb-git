package de.adito.git.nbm.observables;

import de.adito.git.nbm.util.ProjectUtility;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
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
      Observable<Optional<Project>> activeProjectObs = Observable.create(new ActiveProjectObservable())
          .distinctUntilChanged()
          .filter(Optional::isPresent)
          .startWith(_findProjectFromActives(TopComponent.getRegistry()));
      observableRef = Observable.combineLatest(activeProjectObs, OpenProjectsObservable.create(),
                                               (pOptionalProject, pProjects) -> pOptionalProject.map(pProject -> pProjects.contains(pProject) ? pProject : null))
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
    TopComponent activatedTopComponent = pTopComponentRegistry.getActivated();
    if (activatedTopComponent == null)
      return Optional.empty();
    Project project = activatedTopComponent.getLookup().lookup(Project.class);
    if (project == null)
    {
      project = _getProjectFromActiveNodes(pTopComponentRegistry);
    }
    if (project == null)
    {
      project = _getProjectFromTopComponentNodes(activatedTopComponent);
    }
    return Optional.ofNullable(project);
  }

  /**
   * tries to find a project in the nodes from the active TopComponent
   *
   * @param pActivatedTopComponent Netbeans TopComponentRegistry
   * @return Project if any was found, null otherwise
   */
  @Nullable
  private static Project _getProjectFromTopComponentNodes(TopComponent pActivatedTopComponent)
  {
    Node foundNode = pActivatedTopComponent.getLookup().lookup(Node.class);
    if (foundNode != null)
      return ProjectUtility.findProject(foundNode);
    return null;
  }

  /**
   * tries to find a project in the activated nodes from the topComponentRegistry
   *
   * @param pTopComponentRegistry Netbeans TopComponentRegistry
   * @return Project if any was found, null otherwise
   */
  @Nullable
  private static Project _getProjectFromActiveNodes(TopComponent.Registry pTopComponentRegistry)
  {
    Optional<Project> projectFromActiveNodes = Arrays.stream(pTopComponentRegistry.getActivatedNodes())
        .map(pActiveNode -> pActiveNode.getLookup().lookup(Project.class))
        .filter(Objects::nonNull)
        .findFirst();
    return projectFromActiveNodes.orElse(null);
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
