package de.adito.git.nbm.observables;

import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
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

  private static Observable<List<Project>> instance;

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
  @NotNull
  public static synchronized Observable<List<Project>> create()
  {
    if (instance == null)
      instance = Observable.create(new OpenProjectsObservable())
          .startWithItem(List.of(OpenProjects.getDefault().getOpenProjects()))
          .replay(1)
          .autoConnect();
    return instance;
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull OpenProjects pOpenProjects, @NotNull IFireable<List<Project>> pFireable)
  {
    PropertyChangeListener pcl = evt -> {
      if (Objects.equals(evt.getPropertyName(), OpenProjects.PROPERTY_OPEN_PROJECTS))
        pFireable.fireValueChanged(List.of(pOpenProjects.getOpenProjects()));
    };
    pOpenProjects.addPropertyChangeListener(pcl);
    return pcl;
  }

  @Override
  protected void removeListener(@NotNull OpenProjects pOpenProjects, @NotNull PropertyChangeListener pPropertyChangeListener)
  {
    pOpenProjects.removePropertyChangeListener(pPropertyChangeListener);
  }

}