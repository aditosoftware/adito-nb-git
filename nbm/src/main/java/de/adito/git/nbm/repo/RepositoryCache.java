package de.adito.git.nbm.repo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.nbm.Guice.IRepositoryProvider;
import de.adito.git.nbm.Guice.IRepositoryProviderFactory;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a cache for all repositorys. If a project has a repository the cache put it in his own map.
 * This allow to check all repositorys.
 *
 * @author a.arnold, 22.10.2018
 */
public class RepositoryCache {

    private static final Logger LOGGER = Logger.getLogger(RepositoryCache.class.getName());
    private static RepositoryCache INSTANCE;
    private PropertyChangeListener pcl = new _OpenProjectListener();
    private boolean initiated = false;
    private Injector INJECTOR = Guice.createInjector(new AditoNbmModule());
    private IRepositoryProviderFactory repositoryProviderFactory = INJECTOR.getInstance(IRepositoryProviderFactory.class);
    private Map<Project, IRepositoryProvider> REPOSITORYCACHE = new HashMap<>();

    /**
     * Constructor for single instance.
     *
     * @return The {@link RepositoryCache}
     */
    public static RepositoryCache getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RepositoryCache();
        return INSTANCE;
    }

    /**
     * Initialise the PropertyChangeListener for the project.
     */
    public void init() {
        OpenProjects.getDefault().addPropertyChangeListener(pcl);
        initiated = true;
    }

    /**
     * Delete the PropertyChangeListener when the project is closed.
     */
    public void clear() {
        OpenProjects.getDefault().removePropertyChangeListener(pcl);
        initiated = false;
    }

    /**
     * @param pProject The Project for which the repository should be returned
     * @return The repository of the project
     */
    @NotNull
    public Observable<Optional<IRepository>> findRepository(@NotNull Project pProject) {
        if (!initiated)
            throw new RuntimeException("Repository Cache not initialized yet!");

        IRepositoryProvider provider = REPOSITORYCACHE.get(pProject);
        if (provider == null)
            return BehaviorSubject.createDefault(Optional.empty());
        return provider.getRepositoryImpl();
    }

    /**
     * The update method for the REPOSITORYCHACHE. If a project will be closed the update
     * method deletes the repository in the REPOSITORYCACHE. The same action with opening and add a project.
     */
    private void _update() {
        List<Project> projects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());

        // Delete old cached projects
        for (Project cachedPrj : new HashSet<>(REPOSITORYCACHE.keySet())) {
            if (!projects.contains(cachedPrj))
                _doOnProjectClose(cachedPrj);
        }

        // Add opened projects
        for (Project openedProject : projects) {
            if (!REPOSITORYCACHE.containsKey(openedProject))
                try {
                    _doOnProjectOpened(openedProject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * add a repository to the REPOSITORYCACHE
     *
     * @param pProject the project to add
     */
    private void _doOnProjectOpened(Project pProject) {
        try {
            REPOSITORYCACHE.put(pProject, repositoryProviderFactory.create(_getDescription(pProject)));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     * remove a repository from the REPOSITORYCACHE
     *
     * @param pProject the project to remove
     */
    private void _doOnProjectClose(Project pProject) {
        try {
            REPOSITORYCACHE.remove(pProject);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     * @return return a new {@link ProjectRepositoryDescription} for the project
     */
    private IRepositoryDescription _getDescription(Project pProject) {
        return new ProjectRepositoryDescription(pProject);
    }

    /**
     * Listener on OpenProjectsW
     */
    private class _OpenProjectListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                _update();
            }
        }
    }
}
