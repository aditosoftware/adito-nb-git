package de.adito.git.nbm.repo;

import com.google.inject.*;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.nbm.guice.*;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

import java.beans.*;
import java.util.*;
import java.util.logging.*;

/**
 * This class is a cache for all Repositories. If a project has a repository the cache put it in his own map.
 * This allow to check all Repositories.
 *
 * @author a.arnold, 22.10.2018
 */
public class RepositoryCache
{

  private static final Logger LOGGER = Logger.getLogger(RepositoryCache.class.getName());
  private static RepositoryCache instance;
  private PropertyChangeListener pcl = new _OpenProjectListener();
  private boolean initiated = false;
  private Injector injector = Guice.createInjector(new AditoNbmModule());
  private IRepositoryProviderFactory repositoryProviderFactory = injector.getInstance(IRepositoryProviderFactory.class);
  private Map<Project, IRepositoryProvider> repoCache = new HashMap<>();

  /**
   * Constructor for single instance.
   *
   * @return The {@link RepositoryCache}
   */
  public static RepositoryCache getInstance()
  {
    if (instance == null)
      instance = new RepositoryCache();
    return instance;
  }

  /**
   * Initialise the PropertyChangeListener for the project.
   */
  public void init()
  {
    OpenProjects.getDefault().addPropertyChangeListener(pcl);
    initiated = true;
  }

  /**
   * Delete the PropertyChangeListener when the project is closed.
   */
  public void clear()
  {
    OpenProjects.getDefault().removePropertyChangeListener(pcl);
    initiated = false;
  }

  /**
   * @param pProject The Project for which the repository should be returned
   * @return The repository of the project
   */
  @NotNull
  public Observable<Optional<IRepository>> findRepository(@NotNull Project pProject)
  {
    if (!initiated)
      throw new RuntimeException("Repository Cache not initialized yet!");

    IRepositoryProvider provider = repoCache.get(pProject);
    if (provider == null)
      return BehaviorSubject.createDefault(Optional.empty());
    return provider.getRepositoryImpl();
  }

  /**
   * Listener on OpenProjectsW
   */
  private class _OpenProjectListener implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pEvent)
    {
      if (pEvent.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS))
      {
        _update();
      }
    }

    /**
     * The update method for the REPOSITORYCHACHE. If a project will be closed the update
     * method deletes the repository in the repoCache. The same action with opening and add a project.
     */
    private void _update()
    {
      List<Project> projects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());

      // Delete old cached projects
      for (Project cachedPrj : new HashSet<>(repoCache.keySet()))
      {
        if (!projects.contains(cachedPrj))
          _doOnProjectClose(cachedPrj);
      }

      // Add opened projects
      for (Project openedProject : projects)
      {
        if (!repoCache.containsKey(openedProject))
        {
          try
          {
            _doOnProjectOpened(openedProject);
          }
          catch (Exception e)
          {
            throw new RuntimeException(e);
          }
        }
      }
    }

    /**
     * add a repository to the repoCache
     *
     * @param pProject the project to add
     */
    private void _doOnProjectOpened(Project pProject)
    {
      try
      {
        repoCache.put(pProject, repositoryProviderFactory.create(_getDescription(pProject)));
      }
      catch (Exception e)
      {
        LOGGER.log(Level.SEVERE, "", e);
      }
    }

    /**
     * remove a repository from the repoCache
     *
     * @param pProject the project to remove
     */
    private void _doOnProjectClose(Project pProject)
    {
      try
      {
        repoCache.remove(pProject);
      }
      catch (Exception e)
      {
        LOGGER.log(Level.SEVERE, "", e);
      }
    }

    /**
     * @return return a new {@link ProjectRepositoryDescription} for the project
     */
    private IRepositoryDescription _getDescription(Project pProject)
    {
      return new ProjectRepositoryDescription(pProject);
    }
  }
}
