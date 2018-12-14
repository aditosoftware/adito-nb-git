package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.impl.data.FileChangeTypeImpl;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 14.12.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.ResolveConflictsNBAction")
@ActionRegistration(displayName = "LBL_ShowAllBranchesNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.RESOLVE_CONFLICTS_ACTION_RIGHT_CLICK)
public class ResolveConflictsNBAction extends NBAction
{
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    Optional<List<IFileChangeType>> fileChangeTypeList;
    fileChangeTypeList = Optional.of(repository.blockingFirst().orElseThrow()
                                         .getStatus().blockingFirst()
                                         .map(IFileStatus::getConflicting).orElse(Collections.emptySet())
                                         .stream().map(pFilePath -> new FileChangeTypeImpl(new File(pFilePath), EChangeType.CONFLICTING))
                                         .collect(Collectors.toList()));
    actionProvider.getResolveConflictsAction(repository, Observable.just(fileChangeTypeList)).actionPerformed(null);
  }

  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    File projectDir = repository.blockingFirst().map(IRepository::getTopLevelDirectory).orElse(null);
    if (projectDir == null)
      return false;
    Optional<List<File>> conflictingFiles = repository.blockingFirst()
        .map(pRepo -> pRepo.getStatus().blockingFirst()
            .map(IFileStatus::getConflicting).orElse(Collections.emptySet()).stream()
            .map(pFilePath -> new File(projectDir, pFilePath)).collect(Collectors.toList()));
    return conflictingFiles.map(pConflictingFiles -> pConflictingFiles.stream()
        .anyMatch(pConflictingFile -> getAllFilesOfNodes(pActivatedNodes).stream()
            .anyMatch(pFile -> pFile.equals(pConflictingFile)))).orElse(false);
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(ResolveConflictsNBAction.class, "LBL_ResolveConflictsNBAction_Name");
  }
}
