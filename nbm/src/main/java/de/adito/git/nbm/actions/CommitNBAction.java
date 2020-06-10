package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An action class which opens the commit dialog, responsible for the action in the right-click menu
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.CommitNBAction")
@ActionRegistration(displayName = "#LBL_CommitNBAction_Name")
@ActionReference(name = "Commit", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.COMMIT_ACTION_RIGHT_CLICK)
public class CommitNBAction extends NBAction
{

  /**
   * open the commit dialog if the repository is notNull.
   *
   * @param pActivatedNodes the activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {


    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    Subject<Optional<List<IFileChangeType>>> listNodes;

    if (pActivatedNodes.length == 0)
    {
      listNodes = BehaviorSubject.createDefault(Optional.empty());
    }
    else
    {
      listNodes = BehaviorSubject.createDefault(getUncommittedFilesOfNodes(pActivatedNodes, repository));
    }
    actionProvider.getCommitAction(repository, listNodes, "").actionPerformed(null);
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_CommitNBAction_Path");
  }

  /**
   * @param pRepository the currently active repository
   * @return return true if the nodes have one repository and there are files which are not committed.
   */
  private boolean isEnabled(@Nullable IRepository pRepository)
  {
    if (pRepository == null)
      return false;
    boolean containsUncommittedFiles = !pRepository.getStatus().blockingFirst().map(pStatus -> pStatus.getUncommitted().isEmpty()).orElse(true);
    Boolean canCommit = pRepository.getRepositoryState().blockingFirst().map(IRepositoryState::canCommit).orElse(false);
    if (SwingUtilities.isEventDispatchThread())
    {
      _updateTooltip(canCommit, containsUncommittedFiles);
    }
    else
    {
      SwingUtilities.invokeLater(() -> _updateTooltip(canCommit, containsUncommittedFiles));
    }
    return canCommit && containsUncommittedFiles;
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(CommitNBAction.class, "LBL_CommitNBAction_Name");
  }

  /**
   * update the current Tooltip, to give the user additional clues why the action is currently disabled if that is the case
   *
   * @param pCanCommit                if the current repository state allows committing
   * @param pContainsUncommittedFiles if there are uncommited files in the repository
   */
  private void _updateTooltip(Boolean pCanCommit, boolean pContainsUncommittedFiles)
  {
    List<String> tooltipInfos = new ArrayList<>();
    if (!pContainsUncommittedFiles)
      tooltipInfos.add(NbBundle.getMessage(CommitNBAction.class, "Commit_NoUnCommittedFiles"));
    if (!pCanCommit)
      tooltipInfos.add(NbBundle.getMessage(CommitNBAction.class, "Commit_CannotCommit"));
    if (tooltipInfos.isEmpty())
      tooltipInfos.add(getName());
    putValue(Action.SHORT_DESCRIPTION, "<html>" + StringUtils.join(tooltipInfos, "<br>") + "</html>");

  }
}
