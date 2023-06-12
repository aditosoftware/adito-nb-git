package de.adito.git.nbm.observables;

import de.adito.util.reactive.AbstractListenerObservable;
import de.adito.util.reactive.cache.ObservableCache;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Observable, welches auf die OpenProjects hört und diese als Liste ausgibt wenn sich diese verändert.
 *
 * @author w.glanzer, 24.12.2018
 */
public class OpenProjectsObservable extends AbstractListenerObservable<PropertyChangeListener, OpenProjects, List<Project>>
{

  private static final ObservableCache _CACHE = new ObservableCache();

  private OpenProjectsObservable()
  {
    super(OpenProjects.getDefault());
  }

  /**
   * Erstellt ein Observable, welches auf die geöffneten Projekte hört und diese als Liste zurückgibt.
   * Wird ein Projekt geöffnet oder geschlossen, feuert es eine neue Liste
   *
   * @return das Observable mit einer unmodifizierbaren Liste der Projekte
   */
  @NonNull
  public static synchronized Observable<List<Project>> create()
  {
    return _CACHE.calculateParallel("create", () -> Observable.create(new OpenProjectsObservable())
        .startWithItem(List.of(OpenProjects.getDefault().getOpenProjects())));
  }

  @NonNull
  @Override
  protected PropertyChangeListener registerListener(@NonNull OpenProjects pOpenProjects, @NonNull IFireable<List<Project>> pFireable)
  {
    PropertyChangeListener pcl = evt -> {
      if (Objects.equals(evt.getPropertyName(), OpenProjects.PROPERTY_OPEN_PROJECTS))
        pFireable.fireValueChanged(List.of(pOpenProjects.getOpenProjects()));
    };
    pOpenProjects.addPropertyChangeListener(pcl);
    return pcl;
  }

  @Override
  protected void removeListener(@NonNull OpenProjects pOpenProjects, @NonNull PropertyChangeListener pPropertyChangeListener)
  {
    pOpenProjects.removePropertyChangeListener(pPropertyChangeListener);
  }

}