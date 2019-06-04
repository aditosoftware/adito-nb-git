package de.adito.git.nbm.vcs;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.util.reactive.ObservableCollectors;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.util.NbPreferences;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author a.arnold, 30.10.2018
 */
@SuppressWarnings("unused")
@VersioningSystem.Registration(displayName = "Git", menuLabel = "Git", metadataFolderNames = ".git", actionsCategory = "Git")
public class GitVersioningSystemImpl extends VersioningSystem implements IDiscardable
{

  private final Map<File, EChangeType> changes = new ConcurrentHashMap<>();
  private final Disposable annotationsDisposable;
  private VCSAnnotator annotator;

  public GitVersioningSystemImpl()
  {
    // central place that gets called early, so set the log level for the git module here
    String logLevel = NbPreferences.forModule(IPrefStore.class).get(Constants.LOG_LEVEL_SETTINGS_KEY, null);
    Logger.getLogger("de.adito.git").setLevel(logLevel != null ? Level.parse(logLevel) : Level.INFO);
    annotationsDisposable = RepositoryCache.getInstance().repositories()
        .switchMap(pRepoList -> pRepoList.stream()
            .map(IRepository::getStatus)
            .collect(ObservableCollectors.combineOptionalsToList()))
        .subscribe(pFileStatusList -> {
          Map<File, Boolean> statusChanges = _diffWithCurrentStatus(pFileStatusList);

          // Remove old change-entries
          changes.entrySet().iterator().forEachRemaining(pEntry -> {
            if (!statusChanges.containsKey(pEntry.getKey()))
            {
              fireAnnotationsChanged(Set.of(pEntry.getKey()));
              changes.remove(pEntry.getKey());
            }
          });


          // Fire changed files
          Set<File> changedFiles = statusChanges.entrySet().stream()
              .filter(Map.Entry::getValue)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
          if (!changedFiles.isEmpty())
            fireAnnotationsChanged(changedFiles);
        });
  }

  @Override
  public VCSAnnotator getVCSAnnotator()
  {
    if (annotator == null)
      annotator = new GitAnnotator(changes::get);
    return annotator;
  }

  @Override
  public File getTopmostManagedAncestor(File pFile)
  {
    Project project = FileOwnerQuery.getOwner(pFile.toURI());
    if (project == null)
      return null;
    return new File(project.getProjectDirectory().toURI());
  }

  @Override
  public void discard()
  {
    if (!annotationsDisposable.isDisposed())
      annotationsDisposable.dispose();
  }

  @NotNull
  private Map<File, Boolean> _diffWithCurrentStatus(@NotNull List<IFileStatus> pFileStatusList)
  {
    Map<File, Boolean> statusChanges = new HashMap<>();
    for (IFileStatus status : pFileStatusList)
    {
      for (IFileChangeType uncommitted : status.getUncommitted())
      {
        EChangeType previousStatus = changes.put(uncommitted.getFile(), uncommitted.getChangeType());
        statusChanges.put(uncommitted.getFile(), previousStatus != uncommitted.getChangeType());
      }
    }
    return statusChanges;
  }
}
