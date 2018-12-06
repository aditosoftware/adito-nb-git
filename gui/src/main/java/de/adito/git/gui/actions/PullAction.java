package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * pullAction to pull from one branch.
 *
 * @author A.Arnold 11.10.2018
 */
class PullAction extends AbstractAction
{
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;

  /**
   * The PullAction is an action to pull all commits from one branch. If no branch is chosen take an empty string for the master branch.
   *
   * @param pRepository the repository where the pull command should work
   */
  @Inject
  PullAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    dialogProvider = pDialogProvider;
    putValue(Action.NAME, "Pull");
    putValue(Action.SHORT_DESCRIPTION, "Pull all Files from one Branch");
    repository = pRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    Runnable attemptMergeRunnable = this::_doRebase;
    new Thread(attemptMergeRunnable).start();
  }

  /**
   * Keeps calling the pull method of the repository until the result is either a success or the user
   * presses cancel on one of the Conflict resolution dialogs
   */
  private void _doRebase()
  {
    boolean doAbort = false;
    try
    {
      while (!doAbort)
      {
        IRebaseResult rebaseResult =
            repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).pull(false);
        if (rebaseResult.isSuccess())
          break;
        if (!rebaseResult.getMergeConflicts().isEmpty())
        {
          // if the pull should be aborted, _handleConflictDialog returns true
          doAbort = _handleConflictDialog(rebaseResult.getMergeConflicts());
        }
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
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
      IRebaseResult abortedRebaseResult =
          repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).pull(true);
      if (abortedRebaseResult.getResultType() != IRebaseResult.ResultType.ABORTED)
        throw new RuntimeException("The abort of the rebase failed with state: " + abortedRebaseResult.getResultType());
      // user pressed cancel -> abort
      return true;
    }
    return false;
  }
}
