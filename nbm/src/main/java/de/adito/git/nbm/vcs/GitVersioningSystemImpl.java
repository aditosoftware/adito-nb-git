package de.adito.git.nbm.vcs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.impl.util.GitRawTextComparator;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.util.reactive.ObservableCollectors;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.util.NbPreferences;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author a.arnold, 30.10.2018
 */
@SuppressWarnings("unused")
@VersioningSystem.Registration(displayName = "Git", menuLabel = "Git", metadataFolderNames = ".git", actionsCategory = "Git")
public class GitVersioningSystemImpl extends VersioningSystem implements IDiscardable
{

  private final Map<File, EChangeType> changedFiles = new HashMap<>();
  private final Multimap<File, File> aodFileMappings = ArrayListMultimap.create();
  private final Disposable annotationsDisposable;
  private VCSAnnotator annotator;

  public GitVersioningSystemImpl()
  {
    // central place that gets called early, so set the log level for the git module here
    String logLevel = NbPreferences.forModule(IPrefStore.class).get(Constants.LOG_LEVEL_SETTINGS_KEY, null);
    Logger.getLogger("de.adito.git").setLevel(logLevel != null ? Level.parse(logLevel) : Level.INFO);

    GitRawTextComparator.setCurrent(NbPreferences.forModule(IPrefStore.class).get(Constants.RAW_TEXT_COMPARATOR_SETTINGS_KEY, null));

    annotationsDisposable = RepositoryCache.getInstance().repositories()
        .switchMap(pRepoList -> pRepoList.stream()
            .map(IRepository::getStatus)
            .collect(ObservableCollectors.combineOptionalsToList()))
        .subscribe(pFileStatusList -> {
          Set<File> statusChanges = _diffWithCurrentStatus(pFileStatusList);

          if (!statusChanges.isEmpty())
            fireAnnotationsChanged(statusChanges);
        });
  }

  @Override
  public VCSAnnotator getVCSAnnotator()
  {
    if (annotator == null)
      annotator = new GitAnnotator(this::_sychronizedGet);
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

  /**
   * wraps the call to the changed files map in a synchronized block, to make sure the call does not happen during an update cycle
   *
   * @param pFile File whose value from the map should be retrieved
   * @return EChangeType that is stored under the pFile key, or null if no value as stored for pFile
   */
  @Nullable
  private EChangeType _sychronizedGet(@NotNull File pFile)
  {
    synchronized (changedFiles)
    {
      return changedFiles.get(pFile);
    }
  }

  private Set<File> _diffWithCurrentStatus(@NotNull List<IFileStatus> pFileStatusList)
  {
    Set<File> statusChangedFiles = new HashSet<>();
    // gather all currently changed files and their EChangeType
    Map<File, EChangeType> currentChangedFiles = new HashMap<>();
    for (IFileStatus fileStatus : pFileStatusList)
    {
      fileStatus.getUncommitted().forEach(pFileChangeType -> currentChangedFiles.put(pFileChangeType.getFile(), pFileChangeType.getChangeType()));
    }

    synchronized (this.changedFiles)
    {
      // Remove old change-entries
      _removeOldEntries(statusChangedFiles, currentChangedFiles);

      // add new/changed entries
      _addChangedFiles(statusChangedFiles, currentChangedFiles);
    }
    return statusChangedFiles;
  }

  /**
   * Removes all entries that no longer fit from the changedFiles map
   *
   * @param pStatusChangedFiles Set of files that did change their status since the last call, will be filled by this method
   * @param pChangedFiles       Map of Files and their ChangeType, this represents the status as it is now as reported by JGit
   */
  private void _removeOldEntries(Set<File> pStatusChangedFiles, Map<File, EChangeType> pChangedFiles)
  {
    List<File> filesToRemove = new ArrayList<>();
    for (Map.Entry<File, EChangeType> changeTypeEntry : changedFiles.entrySet())
    {
      // if the file is not in the changed files AND is not a key of the aodFileMappings (that would means it itself is not changed, but another file that has
      // the aod as reference is changed and that's why the file is in the changes files)
      if (!pChangedFiles.containsKey(changeTypeEntry.getKey()) && !aodFileMappings.containsKey(changeTypeEntry.getKey()))
      {
        pStatusChangedFiles.add(changeTypeEntry.getKey());
        filesToRemove.add(changeTypeEntry.getKey());
        // if the file is not an aod file, find out its corresponding aod file
        if (!changeTypeEntry.getKey().getName().endsWith(".aod"))
        {
          _removeCorrespondingAOD(pStatusChangedFiles, pChangedFiles, filesToRemove, changeTypeEntry);
        }
      }
    }
    filesToRemove.forEach(changedFiles::remove);
  }

  /**
   * removes the correspoding aod file for a given file from the list of changed files (if the aod file itself is not changed, or is not still referenced by another file)
   *
   * @param pStatusChangedFiles Set of files that did change their status since the last call, will be filled by this method
   * @param pChangedFiles       Map of Files and their ChangeType, this represents the status as it is now as reported by JGit
   * @param pFilesToRemove      List of files that should be removed from the central list of changed files, an entry may be added by this method
   * @param changeTypeEntry     Entry that was removed from the list of files that changed their status and for which the corresponding aod file should be found/removed
   */
  private void _removeCorrespondingAOD(@NotNull Set<File> pStatusChangedFiles, @NotNull Map<File, EChangeType> pChangedFiles, @NotNull List<File> pFilesToRemove,
                                       @NotNull Map.Entry<File, EChangeType> changeTypeEntry)
  {
    File aodFile = _getAODFile(changeTypeEntry.getKey(), null);
    if (aodFile != null)
    {
      // if the aod file is not itself a changed file and no other file references the aod file, remove the aod file from the changed files
      if (aodFileMappings.get(aodFile).size() == 1 && !pChangedFiles.containsKey(aodFile))
      {
        pFilesToRemove.add(aodFile);
        // aod File removed from changed files -> it switched status
        pStatusChangedFiles.add(aodFile);
        // remove aodFile from the aodFileMappings
        aodFileMappings.removeAll(aodFile);
      }
      else
      {
        // remove reference from the one file to the aod file from the aodFileMappings
        aodFileMappings.remove(aodFile, changeTypeEntry.getKey());
      }
    }
  }

  /**
   * adds all new or changed files to the changedFiles map
   *
   * @param pStatusChangedFiles Set of files that did change their status since the last call, will be filled by this method
   * @param pChangedFiles       Map of Files and their ChangeType, this represents the status as it is now as reported by JGit
   */
  private void _addChangedFiles(Set<File> pStatusChangedFiles, Map<File, EChangeType> pChangedFiles)
  {
    for (Map.Entry<File, EChangeType> changedFileEntry : pChangedFiles.entrySet())
    {
      // if the file is not yet in the changed files or the file is in the changed files with another changetype
      if (!changedFiles.containsKey(changedFileEntry.getKey()) || !changedFiles.get(changedFileEntry.getKey()).equals(changedFileEntry.getValue()))
      {
        pStatusChangedFiles.add(changedFileEntry.getKey());
        changedFiles.put(changedFileEntry.getKey(), changedFileEntry.getValue());
        // if the changed file is not an aod file find the corresponding aod file
        if (!changedFileEntry.getKey().getName().endsWith(".aod"))
        {
          _addCorrespondingAOD(pStatusChangedFiles, changedFileEntry);
        }
      }
    }
  }

  /**
   * Adds the corresponding aod file for the given file to the changed files
   *
   * @param pStatusChangedFiles Set of files that did change their status since the last call, will be filled by this method
   * @param changedFileEntry    Entry that was added to the list of files that changed their status and for which the corresponding aod file should be found/added
   */
  private void _addCorrespondingAOD(@NotNull Set<File> pStatusChangedFiles, @NotNull Map.Entry<File, EChangeType> changedFileEntry)
  {
    File aodFile = _getAODFile(changedFileEntry.getKey(), null);
    // if the aodFileMappings does not yet know of that aod file, add it to the changed files and add a mapping from the changed file to the aod into the
    // aodMappingMap
    if (aodFile != null)
    {
      // if the aodFile is not yet in the aodFileMapping add the aod file to the changedFiles as well as to the set of files that changed just now
      if (!aodFileMappings.containsKey(aodFile))
      {
        pStatusChangedFiles.add(aodFile);
        changedFiles.put(aodFile, changedFileEntry.getValue());
      }
      // save the reference of the changedFileEntry to the aodFile in the aodFileMappings (so the aod doesnt get removed prematurely)
      aodFileMappings.put(aodFile, changedFileEntry.getKey());
    }
  }

  /**
   * @param pChangedFile   file that changed and is not an aod file
   * @param pProjectFolder top-level folder of the project that the git repository is for
   * @return the aod file that is contained in the closest possible parent folder of the changed file, or null if none can be found
   */
  @Nullable
  private File _getAODFile(@NotNull File pChangedFile, File pProjectFolder)
  {
    if (pChangedFile.getParentFile() != null && !pChangedFile.getParentFile().equals(pProjectFolder))
    {
      File[] filesInDir = pChangedFile.getParentFile().listFiles();
      if (filesInDir != null)
      {
        // check if any file in this folder is an aod file
        for (File file : filesInDir)
        {
          if (file.getName().endsWith(".aod"))
            return file;
        }
        // no aod file in this folder, go up one folder
        return _getAODFile(pChangedFile.getParentFile(), pProjectFolder);
      }
    }
    return null;
  }
}
