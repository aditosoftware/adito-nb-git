package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.progress.*;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * PullAction to pull from one branch.
 *
 * @author A.Arnold 11.10.2018
 */
class PullAction extends AbstractAction
{
  private static final String NO_VALID_REPO_MSG = "no valid repository found";
  private final Observable<Optional<IRepository>> repository;
  private final IDialogProvider dialogProvider;
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;

  /**
   * The PullAction is an action to pull all commits from one branch. If no branch is chosen take an empty string for the master branch.
   *
   * @param pRepository the repository where the pull command should work
   */
  @Inject
  PullAction(IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade,
             @Assisted Observable<Optional<IRepository>> pRepository)
  {
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    putValue(Action.NAME, "Pull");
    putValue(Action.SHORT_DESCRIPTION, "Pull all changes from the remote Branch");
    repository = pRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Updating Project...", this::_doRebase);
  }

  /**
   * Keeps calling the pull method of the repository until the result is either a success or the user
   * presses cancel on one of the Conflict resolution dialogs
   */
  private void _doRebase(@NotNull IProgressHandle pProgressHandle)
  {
    pProgressHandle.setDescription("Retrieving Repository");
    IRepository pRepo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_VALID_REPO_MSG));
    boolean doAbort = false;
    String stashedCommitId = null;
    try
    {
      if (!pRepo.getStatus().blockingFirst().map(pStatus -> pStatus.getUncommitted().isEmpty()).orElse(true))
      {
        pProgressHandle.setDescription("Stashing Changes");
        stashedCommitId = pRepo.stashChanges();
      }
      while (!doAbort)
      {
        pProgressHandle.setDescription("Rebasing");
        IRebaseResult rebaseResult =
            pRepo.pull(false);
        if (rebaseResult.isSuccess())
        {
          break;
        }
        if (!rebaseResult.getMergeConflicts().isEmpty())
        {
          pProgressHandle.setDescription("Resolving Conflicts");
          // if the pull should be aborted, _handleConflictDialog returns true
          doAbort = _handleConflictDialog(rebaseResult.getMergeConflicts());
        }
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if (stashedCommitId != null)
      {
        pProgressHandle.setDescription("Unstashing Changes");
        _doUnStashing(stashedCommitId);
      }
    }
  }

  /**
   * @param pStashedCommitId sha-1 id of the stashed commit to un-stash
   */
  private void _doUnStashing(String pStashedCommitId)
  {
    IRepository pRepo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_VALID_REPO_MSG));
    try
    {
      List<IMergeDiff> stashConflicts = pRepo.unStashChanges(pStashedCommitId);
      if (!stashConflicts.isEmpty())
      {
        DialogResult dialogResult = dialogProvider.showMergeConflictDialog(repository, stashConflicts);
        if (dialogResult.isPressedOk())
        {
          pRepo.dropStashedCommit(pStashedCommitId);
        }
      }
    }
    catch (AditoGitException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * Handles showing the conflict dialog if a conflict occurred during the pull
   *
   * @param pMergeConflicts List of IMergeDiff for each file that is in a conflicting state
   * @return true if the user pressed cancel and the pull should be aborted, false otherwise
   * @throws AditoGitException if an error occurred during the pull
   */
  private boolean _handleConflictDialog(List<IMergeDiff> pMergeConflicts) throws AditoGitException
  {
    DialogResult dialogResult = dialogProvider.showMergeConflictDialog(repository, pMergeConflicts);
    if (!dialogResult.isPressedOk())
    {
      // user pressed cancel -> abort
      IRebaseResult abortedRebaseResult =
          repository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_VALID_REPO_MSG)).pull(true);
      if (abortedRebaseResult.getResultType() != IRebaseResult.ResultType.ABORTED)
        throw new RuntimeException("The abort of the rebase failed with state: " + abortedRebaseResult.getResultType());
      // abort was successful -> notify user
      notifyUtil.notify("Aborted rebase", "Rebase abort was successful, all local content restored to state before rebase", true);
      return true;
    }
    return false;
  }
}
