package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.api.data.IRebaseResult;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.MissingTrackedBranchException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * PullAction to pull from one branch.
 *
 * @author A.Arnold 11.10.2018
 */
class PullAction extends AbstractAction
{
  private static final String STASH_ID_KEY = "pull::stashCommitId";
  private static final String NO_VALID_REPO_MSG = "no valid repository found";
  private final Observable<Optional<IRepository>> repository;
  private final IPrefStore prefStore;
  private final IDialogProvider dialogProvider;
  private final IActionProvider actionProvider;
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final ISaveUtil saveUtil;

  /**
   * The PullAction is an action to pull all commits from one branch. If no branch is chosen take an empty string for the master branch.
   *
   * @param pRepository the repository where the pull command should work
   */
  @Inject
  PullAction(IPrefStore pPrefStore, IDialogProvider pDialogProvider, IActionProvider pActionProvider, INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade,
             ISaveUtil pSaveUtil, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    prefStore = pPrefStore;
    dialogProvider = pDialogProvider;
    actionProvider = pActionProvider;
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    saveUtil = pSaveUtil;
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
    saveUtil.saveUnsavedFiles();
    pProgressHandle.setDescription("Retrieving Repository");
    IRepository pRepo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_VALID_REPO_MSG));
    boolean doAbort = false;
    try
    {
      ICommit head = pRepo.getCommit(null);
      if (pRepo.getStatus().blockingFirst().map(pStatus -> !pStatus.getConflicting().isEmpty()).orElse(false))
      {
        notifyUtil.notify("Found conflicting files", "There are files that have the conflicting state, resolve these conflicts first before pulling", false);
        return;
      }
      if (!pRepo.getStatus().blockingFirst().map(pStatus -> pStatus.getUncommitted().isEmpty()).orElse(true))
      {
        if (!ActionUtility.handleStash(prefStore, dialogProvider, pRepo, STASH_ID_KEY, pProgressHandle))
          return;
      }
      while (!doAbort)
      {
        pProgressHandle.setDescription("Rebasing");
        IRebaseResult rebaseResult =
            pRepo.pull(false);
        if (rebaseResult.isSuccess())
        {
          if (rebaseResult.getResultType() != IRebaseResult.ResultType.UP_TO_DATE)
            notifyUtil.notify("Pull successful", "The pull --rebase was successful, files are now up-to-date", false,
                              actionProvider.getDiffCommitToHeadAction(repository, Observable.just(Optional.of(List.of(head))), Observable.just(Optional.empty())));
          else
          {
            notifyUtil.notify("Pull successful", "Files are already up-to-date", false);
          }
          break;
        }
        if (!rebaseResult.getMergeConflicts().isEmpty())
        {
          pProgressHandle.setDescription("Resolving Conflicts");
          // if the pull should be aborted, _handleConflictDialog returns true
          doAbort = _handleConflictDialog(pRepo, rebaseResult.getMergeConflicts());
        }
        if (rebaseResult.getResultType() == null)
        {
          notifyUtil.notify("Unclear rebase state, aborting rebase", "The current rebase state is neither a conflict nor success, aborting rebase",
                            true);
          _abortRebase(pRepo);
          doAbort = true;
        }
      }
    }
    catch (MissingTrackedBranchException pE)
    {
      notifyUtil.notify(pE, "Pull failed.", false);
    }
    catch (Exception e)
    {
      notifyUtil.notify(e, "Pull failed due to an exception.", false);
    }
    finally
    {
      String stashedCommitId = prefStore.get(STASH_ID_KEY);
      if (stashedCommitId != null)
      {
        pProgressHandle.setDescription("Un-stashing changes");
        StashCommand.doUnStashing(dialogProvider, stashedCommitId, Observable.just(repository.blockingFirst()));
        prefStore.put(STASH_ID_KEY, null);
      }
    }
  }

  /**
   * Handles showing the conflict dialog if a conflict occurred during the pull
   *
   * @param pMergeConflicts List of IMergeDiff for each file that is in a conflicting state
   * @return true if the user pressed cancel and the pull should be aborted, false otherwise
   * @throws AditoGitException if an error occurred during the pull
   */
  private boolean _handleConflictDialog(IRepository pRepo, List<IMergeDiff> pMergeConflicts) throws AditoGitException
  {
    IMergeConflictDialogResult dialogResult = dialogProvider.showMergeConflictDialog(Observable.just(Optional.of(pRepo)), pMergeConflicts, true);
    if (!dialogResult.isFinishMerge())
    {
      _abortRebase(pRepo);
      return true;
    }
    return false;
  }

  /**
   * aborts the rebase
   *
   * @param pRepo Repository to call the aborted rebase on
   * @throws AditoGitException if an error occurs during the abort
   */
  private void _abortRebase(IRepository pRepo) throws AditoGitException
  {
    // user pressed cancel -> abort
    IRebaseResult abortedRebaseResult =
        pRepo.pull(true);
    if (abortedRebaseResult.getResultType() != IRebaseResult.ResultType.ABORTED)
    {
      String errorMessage = "The abort of the rebase failed with state: " + abortedRebaseResult.getResultType();
      RuntimeException pE = new RuntimeException(errorMessage);
      notifyUtil.notify(pE, errorMessage + ". ", false);
    }
    // abort was successful -> notify user
    notifyUtil.notify("Aborted rebase", "Rebase abort was successful, all local content restored to state before rebase", true);
  }
}
