package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.impl.data.FileChangeTypeImpl;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.jetbrains.annotations.*;
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
@ActionRegistration(displayName = "#LBL_ResolveConflictsNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.RESOLVE_CONFLICTS_ACTION_RIGHT_CLICK,
    separatorAfter = INBActionPositions.RESOLVE_CONFLICTS_ACTION_RIGHT_CLICK + 1)
public class ResolveConflictsNBAction extends NBAction
{
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    Optional<List<IFileChangeType>> fileChangeTypeList = Optional.of(repository.blockingFirst().orElseThrow()
                                         .getStatus().blockingFirst()
                                         .map(IFileStatus::getConflicting).orElse(Collections.emptySet())
                                         .stream().map(pFilePath -> new FileChangeTypeImpl(new File(pFilePath), new File(pFilePath), EChangeType.CONFLICTING))
                                         .collect(Collectors.toList()));
    actionProvider.getResolveConflictsAction(repository, Observable.just(fileChangeTypeList)).actionPerformed(null);
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  private boolean isEnabled(@Nullable IRepository pRepository)
  {
    if (pRepository == null)
      return false;
    File projectDir = pRepository.getTopLevelDirectory();
    if (projectDir == null)
      return false;
    List<File> conflictingFiles = pRepository.getStatus().blockingFirst()
            .map(IFileStatus::getConflicting).orElse(Collections.emptySet()).stream()
        .map(pFilePath -> new File(projectDir, pFilePath)).collect(Collectors.toList());
    return conflictingFiles.stream()
        .anyMatch(pConflictingFile -> getAllFilesOfNodes(lastActivated).stream()
            .anyMatch(pFile -> pConflictingFile.toPath().startsWith(pFile.toPath())));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(ResolveConflictsNBAction.class, "LBL_ResolveConflictsNBAction_Name");
  }
}
