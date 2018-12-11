package de.adito.git.nbm.repo;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.guice.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;

import java.beans.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

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
  private IRepositoryProviderFactory repositoryProviderFactory = IGitConstants.INJECTOR.getInstance(IRepositoryProviderFactory.class);
  private Map<FileObject, RepositoryProvider> repoCache = new WeakHashMap<>();

  private RepositoryCache()
  {
  }

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
    _update();
  }

  /**
   * Delete the PropertyChangeListener when the project is closed.
   */
  public void clear()
  {
    OpenProjects.getDefault().removePropertyChangeListener(pcl);
    for (Project project : OpenProjects.getDefault().getOpenProjects())
      _doOnProjectClose(project.getProjectDirectory());
    repoCache.clear();
  }

  /**
   * @param pProject The Project for which the repository should be returned
   * @return The repository of the project
   */
  @NotNull
  public Observable<Optional<IRepository>> findRepository(@NotNull Project pProject)
  {
    return _getProvider(pProject.getProjectDirectory()).getRepositoryImpl();
  }

  /**
   * add a repository to the repoCache
   *
   * @param pProjectDirectory the project to add
   */
  private void _doOnProjectOpened(FileObject pProjectDirectory)
  {
    try
    {
      RepositoryProvider provider = _getProvider(pProjectDirectory);
      provider.setRepositoryDescription(new ProjectRepositoryDescription(pProjectDirectory));
    }
    catch (Exception e)
    {
      LOGGER.log(Level.SEVERE, "", e);
    }
  }

  /**
   * remove a repository from the repoCache
   *
   * @param pProjectDirectory the project to remove
   */
  private void _doOnProjectClose(FileObject pProjectDirectory)
  {
    try
    {
      RepositoryProvider provider = _getProvider(pProjectDirectory);
      provider.setRepositoryDescription(null);
    }
    catch (Exception e)
    {
      LOGGER.log(Level.SEVERE, "", e);
    }
  }

  @NotNull
  private RepositoryProvider _getProvider(FileObject pProjectDirectory)
  {
    return repoCache.computeIfAbsent(pProjectDirectory, pProj -> repositoryProviderFactory.create(null));
  }

  /**
   * The update method for the REPOSITORYCHACHE. If a project will be closed the update
   * method deletes the repository in the repoCache. The same action with opening and add a project.
   */
  private void _update()
  {
    List<FileObject> projectFolders = Stream.of(OpenProjects.getDefault().getOpenProjects())
        .map(Project::getProjectDirectory)
        .collect(Collectors.toList());

    // Delete old cached projects
    for (FileObject cachedPrj : new HashSet<>(repoCache.keySet()))
      if (!projectFolders.contains(cachedPrj))
        _doOnProjectClose(cachedPrj);

    // Add opened projects
    for (FileObject openedProject : projectFolders)
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
   * Listener on OpenProjects
   */
  private class _OpenProjectListener implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pEvent)
    {
      if (pEvent.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS))
        _update();
    }
  }
}
