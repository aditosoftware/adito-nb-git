package de.adito.git.nbm.repo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.adito.git.api.IRepository;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.guice.IRepositoryProviderFactory;
import de.adito.git.nbm.guice.RepositoryProvider;
import de.adito.util.reactive.ObservableCollectors;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a cache for all Repositories. If a project has a repository the cache put it in his own map.
 * This allow to check all Repositories.
 *
 * @author a.arnold, 22.10.2018
 */
public class RepositoryCache
{

  private static final Logger LOGGER = Logger.getLogger(RepositoryCache.class.getName());
  private static final String GIT_FOLDER_NAME = ".git";
  private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(), 30, TimeUnit.SECONDS,
                                                                                   new ArrayBlockingQueue<>(20));
  private static RepositoryCache instance;
  private final PropertyChangeListener openProjectListener = new _OpenProjectListener();
  private final IRepositoryProviderFactory repositoryProviderFactory = IGitConstants.INJECTOR.getInstance(IRepositoryProviderFactory.class);
  private final BehaviorSubject<List<RepositoryProvider>> providers = BehaviorSubject.createDefault(List.of());

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
    OpenProjects.getDefault().addPropertyChangeListener(openProjectListener);
    executorService.submit(RepositoryCache.this::_update);
  }

  public void triggerUpdate()
  {
    executorService.submit(() -> {
      try
      {
        Thread.sleep(5000);
      }
      catch (InterruptedException pE)
      {
      }
      RepositoryCache.getInstance()._update();
    });
  }

  /**
   * Delete the PropertyChangeListener when the project is closed.
   */
  public void clear()
  {
    OpenProjects.getDefault().removePropertyChangeListener(openProjectListener);
    for (Project project : OpenProjects.getDefault().getOpenProjects())
      _doOnProjectClose(project.getProjectDirectory());
    providers.onComplete();
  }

  /**
   * @param pProject The Project for which the repository should be returned
   * @return The repository of the project
   */
  @NotNull
  public Observable<Optional<IRepository>> findRepository(@NotNull Project pProject)
  {
    return providers.switchMap(pRepoProviders -> pRepoProviders.stream()
            .filter(pRepo -> pRepo.getRepositoryFolder().equals(pProject.getProjectDirectory()))
            .map(RepositoryProvider::getRepositoryImpl)
            .findFirst()
            .orElse(Observable.just(Optional.empty())))
        .distinctUntilChanged();
  }

  @NotNull
  public Observable<List<IRepository>> repositories()
  {
    Scheduler gitRepoScheduler = Schedulers.from(Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                                                                                       .setNameFormat("Git-repo-computation-%d")
                                                                                       .build()));
    return providers.switchMap(pRepositoryProviders -> pRepositoryProviders.stream()
        .map(RepositoryProvider::getRepositoryImpl)
        .collect(ObservableCollectors.combineOptionalsToList())
        .observeOn(gitRepoScheduler));
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
      if (_isGitRepository(pProjectDirectory) && !_isDependencyProject(pProjectDirectory))
      {
        RepositoryProvider provider = _getProvider(pProjectDirectory, true);
        assert provider != null;
        provider.setRepositoryDescription(new ProjectRepositoryDescription(pProjectDirectory));
      }
    }
    catch (Exception e)
    {
      LOGGER.log(Level.SEVERE, "", e);
    }
  }

  /**
   * remove a repository from the repoCache
   *
   * @param pProjectFolder the project to remove
   */
  private void _doOnProjectClose(@NotNull FileObject pProjectFolder)
  {
    ArrayList<RepositoryProvider> clonedList = new ArrayList<>(providers.getValue());
    for (RepositoryProvider repositoryProvider : providers.getValue())
    {
      if (repositoryProvider.getRepositoryFolder().equals(pProjectFolder))
      {
        repositoryProvider.getRepositoryImpl().blockingFirst(Optional.empty()).ifPresent(pRepo -> {
          pRepo.discard();
          clonedList.remove(repositoryProvider);
          providers.onNext(clonedList);
        });
      }
    }
  }

  @Nullable
  private RepositoryProvider _getProvider(FileObject pProjectDirectory, boolean pCreate)
  {
    List<RepositoryProvider> currentProviders = providers.getValue();
    for (RepositoryProvider provider : currentProviders)
      if (provider.getRepositoryFolder().equals(pProjectDirectory))
        return provider;

    if (pCreate)
    {
      RepositoryProvider thisProvider = repositoryProviderFactory.create(pProjectDirectory);
      currentProviders = new ArrayList<>(currentProviders);
      currentProviders.add(thisProvider);
      providers.onNext(currentProviders);
      return thisProvider;
    }
    else
      return null;
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
    for (RepositoryProvider cachedProvider : providers.getValue())
    {
      FileObject projectFolder = cachedProvider.getRepositoryFolder();
      if (!projectFolders.contains(projectFolder))
        _doOnProjectClose(projectFolder);
    }

    // Add opened projects
    for (FileObject openedProject : projectFolders)
    {
      RepositoryProvider provider = _getProvider(openedProject, false);
      if (provider == null)
        _doOnProjectOpened(openedProject);
    }
  }

  /**
   * checks if a folder contains a ".git" folder and thus is a git repository
   *
   * @param pProjectDirectory top-level folder of a project
   * @return whether or not pProjectDirectory contains a folder named ".git" and thus is a git repository
   */
  private boolean _isGitRepository(FileObject pProjectDirectory)
  {
    for (FileObject childFolder : pProjectDirectory.getChildren())
    {
      if (GIT_FOLDER_NAME.equals(childFolder.getName()))
        return true;
    }
    return false;
  }

  /**
   * checks if a folder is a dependency folder (of nodejs).
   * Those folders have to be ignored, because we dont want to load the repositories from there
   *
   * @param pProjectDirectory Directory
   * @return true if it is a dependency project inside node_modules
   */
  private boolean _isDependencyProject(FileObject pProjectDirectory)
  {
    for (FileObject fo = pProjectDirectory; fo != null; fo = fo.getParent())
    {
      //todo should not be hardcoded, but here is currently no other way to achieve this behavior
      if (fo.getName().equalsIgnoreCase("node_modules"))
        return true;
    }

    return false;
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
