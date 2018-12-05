package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRebaseResult;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

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
    Runnable attemptMergeRunnable = () -> _doRebase(false);
    new Thread(attemptMergeRunnable).start();
  }

  /**
   * Recursive method that calls the pull method as long as there are merge conflicts/
   * the user didn't press abort or cancel/pull doesn't return a success message
   *
   * @param pDoAbort if the rebase should be aborted
   */
  private void _doRebase(boolean pDoAbort)
  {
    try
    {
      IRebaseResult rebaseResult =
          repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).pull(pDoAbort);
      // if the user aborted the rebase or the rebase finished successfully -> break recursion
      if (pDoAbort && rebaseResult.getResultType() == IRebaseResult.ResultType.ABORTED || rebaseResult.isSuccess())
        return;
      if (!rebaseResult.getMergeConflicts().isEmpty())
      {
        DialogResult dialogResult = dialogProvider.showMergeConflictDialog(repository, rebaseResult.getMergeConflicts());
        if (!dialogResult.isPressedOk())
        {
          // user pressed cancel -> abort
          _doRebase(true);
        }
      }
      // if the user pressed OK try and resume the process
      _doRebase(false);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}
