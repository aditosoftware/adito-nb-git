package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.RevertNBAction")
@ActionRegistration(displayName = "#LBL_RevertNBAction_Name")
//Reference for the menu
@ActionReference(name = "Revert file(s)", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.REVERT_ACTION_RIGHT_CLICK)
public class RevertNBAction extends NBAction
{

  private final Subject<Optional<List<IFileChangeType>>> selectedFiles = BehaviorSubject.create();

  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    selectedFiles.onNext(getUncommittedFilesOfNodes(pActivatedNodes, repository));

    actionProvider.getRevertWorkDirAction(repository, selectedFiles).actionPerformed(null);

  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_RevertNBAction_Path");
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  private boolean isEnabled(@Nullable IRepository pRepository)
  {
    Node[] activatedNodes = lastActivated;
    return !getUncommittedFilesOfNodes(activatedNodes, getCurrentRepository(activatedNodes)).orElse(Collections.emptyList()).isEmpty();
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(RevertNBAction.class, "LBL_RevertNBAction_Name");
  }

}
