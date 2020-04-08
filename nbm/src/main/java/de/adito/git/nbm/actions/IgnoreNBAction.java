package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.impl.data.FileChangeTypeImpl;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An action class for NetBeans which ignores files for the version control system
 *
 * @author a.arnold, 31.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.IgnoreAction")
@ActionRegistration(displayName = "#LBL_IgnoreAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.IGNORE_ACTION_RIGHT_CLICK)
public class IgnoreNBAction extends NBAction
{

  private final Subject<Optional<List<IFileChangeType>>> filesToIgnore = BehaviorSubject.create();

  /**
   * Ignore files in the version control system
   *
   * @param pActivatedNodes the activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IRepository currentRepo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(
        NbBundle.getMessage(IgnoreNBAction.class, "Invalid.RepositoryNotValid")));
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    filesToIgnore.onNext(Optional.of(getUntrackedSelectedFiles(currentRepo, pActivatedNodes)));
    actionProvider.getIgnoreAction(repository, filesToIgnore).actionPerformed(null);
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  protected boolean isEnabled(@Nullable IRepository pRepository)
  {
    if (pRepository == null)
      return false;
    return _getUntrackedFiles(pRepository).stream()
        .anyMatch(untrackedFile -> getAllFilesOfNodes(lastActivated)
            .stream()
            .anyMatch(selectedFile -> selectedFile.toURI()
                .equals(untrackedFile.getFile().toURI())));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(IgnoreNBAction.class, "LBL_IgnoreAction_Name");
  }

  private List<IFileChangeType> _getUntrackedFiles(IRepository pCurrentRepo)
  {
    File projectDir = pCurrentRepo.getTopLevelDirectory();
    return pCurrentRepo
        .getStatus()
        .blockingFirst()
        .map(IFileStatus::getUntracked).orElse(Collections.emptySet())
        .stream()
        .map(pFilePath -> new FileChangeTypeImpl(new File(projectDir, pFilePath), new File(projectDir, pFilePath), EChangeType.NEW))
        .collect(Collectors.toList());
  }

  List<IFileChangeType> getUntrackedSelectedFiles(IRepository pCurrentRepo, Node[] pActivatedNodes)
  {
    return _getUntrackedFiles(pCurrentRepo).stream()
        .filter(untrackedFile -> getAllFilesOfNodes(pActivatedNodes)
            .stream()
            .anyMatch(selectedFile -> selectedFile.toURI()
                .equals(untrackedFile.getFile().toURI())))
        .collect(Collectors.toList());
  }

}
