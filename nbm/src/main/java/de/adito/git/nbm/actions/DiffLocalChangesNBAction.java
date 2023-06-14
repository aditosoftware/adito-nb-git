package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.*;

/**
 * Action for the toolbar/rightClick menu that allows the user to quickly diff the currently opened file
 *
 * @author m.kaspera, 16.03.2019
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.DiffLocalChangesNBAction")
@ActionRegistration(displayName = "#LBL_DiffLocalChangesNBAction_Name")
@ActionReferences({
    @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = INBActionPositions.DIFF_LOCAL_CHANGES_ACTION_TOOLBAR),
    @ActionReference(name = "Diff local changes", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.DIFF_LOCAL_CHANGES_ACTION_RIGHT_CLICK)
})
public class DiffLocalChangesNBAction extends NBAction
{
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    if (pActivatedNodes != null && pActivatedNodes.length == 1)
    {
      Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
      IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
      List<IFileChangeType> changeTypes = _getIFileChangeType(pActivatedNodes, repository);
      if (!changeTypes.isEmpty())
        actionProvider.getDiffToHeadAction(repository, Observable.just(Optional.of(changeTypes)), true).actionPerformed(null);
    }
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  private boolean isEnabled(@Nullable IRepository pRepository)
  {
    try
    {
      if (pRepository != null)
      {
        List<File> filesOfNodes = getAllFilesOfNodes(lastActivated);
        Set<String> uncommittedChanges = pRepository.getStatus().blockingFirst().map(IFileStatus::getUncommittedChanges).orElse(Set.of());
        if (filesOfNodes.size() == 1 && filesOfNodes.get(0).isDirectory())
        {
          for (File filesOfNode : _getFilesOfType(filesOfNodes.get(0), ""))
          {
            if (uncommittedChanges.contains(pRepository.getTopLevelDirectory().toPath().relativize(filesOfNode.toPath()).toString().replace("\\", "/")))
            {
              return true;
            }
          }
        }
        else if (filesOfNodes.size() == 1)
        {
          return uncommittedChanges.contains(pRepository.getTopLevelDirectory().toPath().relativize(filesOfNodes.get(0).toPath()).toString().replace("\\", "/"));
        }
      }
    }
    catch (Exception ignored)
    {
      // If an exception is thrown just return false, since if there is a problem with finding the changed files, the action would probably throw an exception as well
    }
    return false;
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_DiffLocalChangesAction_Path");
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(DiffLocalChangesNBAction.class, "LBL_DiffLocalChangesNBAction_Name");
  }

  /**
   * Checks how/if the file contained in the selected node is changed. If the selected node is for a directory, checks if any aod files are
   * in the directory and if exactly one file matches that criterium, return the IFileChangeType of that file instead
   *
   * @param pActivatedNodes selected nodes
   * @param pRepository     observable with the repository containing the files from the selected nodes
   * @return IFileChangeType of the file in the selected node, or null if no or more than one node are selected
   */
  @NonNull
  private static List<IFileChangeType> _getIFileChangeType(Node[] pActivatedNodes, Observable<Optional<IRepository>> pRepository)
  {
    List<File> filesOfNodes = getAllFilesOfNodes(pActivatedNodes);
    List<IFileChangeType> changeTypes = new ArrayList<>();
    Optional<IRepository> optRepository = pRepository.blockingFirst();
    if (filesOfNodes.size() == 1 && filesOfNodes.get(0).isDirectory() && optRepository.isPresent())
    {
      for (File filesOfNode : _getFilesOfType(filesOfNodes.get(0), ""))
      {
        IFileChangeType changeType = optRepository.map(pRepo -> pRepo.getStatusOfSingleFile(filesOfNode)).orElse(null);
        if (changeType != null && changeType.getChangeType() != EChangeType.SAME)
          changeTypes.add(changeType);
      }
    }
    return changeTypes;
  }

  /**
   * @param pParent File whose children should be searched
   * @param pEnding ending of the file. Can be just the type, but may also be more than that
   * @return List of files (NOTE: not directories!) matching the ending
   */
  @NonNull
  private static List<File> _getFilesOfType(@NonNull File pParent, @NonNull String pEnding)
  {
    File[] matchingFiles = pParent.listFiles(pFile -> pFile.isFile() && pFile.getName().endsWith(pEnding));
    return matchingFiles == null ? List.of() : Arrays.asList(matchingFiles);
  }
}
