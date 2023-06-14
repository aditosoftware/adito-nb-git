package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EPushResult;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.GitTransportFailureException;
import de.adito.git.api.exception.PushRejectedOtherReasonException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IChangeTrackedBranchDialogResult;
import de.adito.git.gui.dialogs.results.IPushDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * pushAction to push all commits to the actual chosen branch.
 * To change the actual branch take the checkout method.
 *
 * @author A.Arnold 11.10.2018
 */
class PushAction extends AbstractAction
{

  private static final String BRANCH_STRING = "branch";
  private static final String FAILURE_HEADER = "Push failed";
  private static final Logger LOGGER = Logger.getLogger(PushAction.class.getName());
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final IDialogProvider dialogProvider;

  /**
   * @param pRepository The repository to push
   */
  @Inject
  PushAction(INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
             @Assisted Observable<Optional<IRepository>> pRepository)
  {
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    putValue(Action.NAME, "Push");
    putValue(Action.SHORT_DESCRIPTION, "Push all commits to the remote-tracking branch");
    repository = pRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    repository.blockingFirst().ifPresent(pRepo -> {
      IRepositoryState repoState = pRepo.getRepositoryState().blockingFirst().orElse(null);
      String remoteName = (repoState != null && repoState.getRemotes().size() == 1) ? repoState.getRemotes().get(0) : null;
      if (repoState != null && repoState.getCurrentRemoteTrackedBranch() == null && repoState.getRemotes().size() > 1)
      {
        // need new ArrayList to convert to List<Object>
        IUserPromptDialogResult<?, Object> result = dialogProvider.showComboBoxDialog("Select remote to push branch to", new ArrayList<>(repoState.getRemotes()));
        if (!result.isOkay())
          return;
        remoteName = (String) result.getInformation();
      }
      List<ICommit> commitList = null;
      if (_handleNonMatchingTrackedBranch(pRepo, repoState))
      {
        notifyUtil.notify("Aborted push", "Aborted push due to user action", false);
        return;
      }
      else
      {
        Future<List<ICommit>> future = progressFacade.executeInBackground("Gathering Information for Push", pHandle -> {
          return _determineUnpushedCommits(pRepo);
        });
        try
        {
          // Could try to implement Future Chaining here, but as-is, the time it takes the future to complete is not noticeable
          commitList = future.get();
        }
        catch (InterruptedException | ExecutionException pE)
        {
          LOGGER.log(Level.WARNING, pE, () -> "Exception while waiting for the job that determines the unpushed commits");
          Thread.currentThread().interrupt();
        }
      }
      if (commitList != null)
      {
        IPushDialogResult<?, Boolean> dialogResult = dialogProvider.showPushDialog(Observable.just(repository.blockingFirst()),
                                                                                   commitList);

        if (dialogResult.isPush())
        {
          GitIndexLockUtil.checkAndHandleLockedIndexFile(pRepo, dialogProvider, notifyUtil);

          final String remoteNameFinal = remoteName;
          progressFacade.executeInBackground("Pushing Commits", pHandle -> {
            _performPush(pHandle, pRepo, dialogResult.getInformation(), remoteNameFinal, repoState, notifyUtil);
          });
        }
      }
      else
      {
        notifyUtil.notify("Error while preparing the push", "An error occurred while setting up the push, check the log for more information", false);
      }

    });

  }

  /**
   * @param pRepo current repo
   * @return List of unpushed commits, or null if any error occurs while determining the unpushed commits
   */
  @NonNull
  private List<ICommit> _determineUnpushedCommits(@NonNull IRepository pRepo)
  {
    try
    {
      return pRepo.getUnPushedCommits();
    }
    catch (AditoGitException pE)
    {
      String errorMessage = "Error while finding un-pushed commits";
      notifyUtil.notify(pE, errorMessage + ". ", false);
      return List.of();
    }
  }

  /**
   * @param pHandle     handle of the progressFacade
   * @param pRepo       current repo
   * @param pIsPushTags determines whether tags are pushed or not
   * @param pRemoteName name of the remote to push to
   * @param repoState   current state of the repo
   */
  static void _performPush(@NonNull IProgressHandle pHandle, @NonNull IRepository pRepo, boolean pIsPushTags,
                           @Nullable String pRemoteName, @Nullable IRepositoryState repoState, @NonNull INotifyUtil pNotifyUtil)
  {
    try
    {

      pHandle.setDescription("Pushing");
      _doPush(pRepo, pIsPushTags, pRemoteName, pNotifyUtil);
      if (pRepo.getRepositoryState().blockingFirst().map(pRepoState -> pRepoState.getCurrentRemoteTrackedBranch() == null).orElse(false) && pRemoteName != null
          && repoState != null)
      {

        pRepo.getConfig().establishTrackingRelationship(repoState.getCurrentBranch().getSimpleName(), repoState.getCurrentBranch().getName(), pRemoteName);
      }
      pNotifyUtil.notify("Push", "Push was successful", true);
    }
    catch (GitTransportFailureException pE)
    {
      if (pE.getCause().getMessage().endsWith("push not permitted"))
      {
        pNotifyUtil.notify(pE, FAILURE_HEADER + " You may have insufficient rights for pushing to the remote. ", false);
      }
      else
      {
        pNotifyUtil.notify(pE, FAILURE_HEADER + " An error occurred during transport, check your credentials and rights on the remote. ",
                           false);
      }
      LOGGER.log(Level.SEVERE, pE, () -> "failed to push to remote");
    }
    catch (PushRejectedOtherReasonException pE)
    {
      pNotifyUtil.notify(pE, "Push failed with status: Rejected other reason. A possible cause can be pushing to a protected branch with insufficient permissions.\n" +
          "Detailed message: " + pE.getMessage(), false);
    }
    catch (AditoGitException pE)
    {
      String errorMessage = "Error while finding un-pushed commits";
      pNotifyUtil.notify(pE, errorMessage + ". ", false);
    }
  }

  /**
   * Checks for and handles the case where a local branch tracks a remote branch with a different name
   *
   * @param pRepo      Repository
   * @param pRepoState current State of the repo
   * @return true if the push should be cancelled, false otherwise
   */
  private boolean _handleNonMatchingTrackedBranch(IRepository pRepo, IRepositoryState pRepoState)
  {
    if (pRepoState != null && pRepoState.getCurrentRemoteTrackedBranch() != null
        && !pRepoState.getCurrentBranch().getActualName().equals(pRepoState.getCurrentRemoteTrackedBranch().getActualName())
        && !"tracking".equals(pRepo.getConfig().get(BRANCH_STRING, pRepoState.getCurrentBranch().getSimpleName(), "pull")))
    {
      IChangeTrackedBranchDialogResult dialogResult = dialogProvider.showChangeTrackedBranchDialog("Name of the local branch and its tracked remote branch ("
                                                                                                       + pRepoState.getCurrentRemoteTrackedBranch().getSimpleName()
                                                                                                       + ") differ, do you want to create a new branch \""
                                                                                                       + pRepoState.getCurrentBranch().getSimpleName()
                                                                                                       + "\" on the remote and track that branch instead?");
      if (dialogResult.isChangeBranch())
      {
        pRepo.getConfig().setValue(BRANCH_STRING, pRepoState.getCurrentBranch().getSimpleName(), "merge", pRepoState.getCurrentBranch().getName());
      }
      else if (dialogResult.isKeepTrackedBranch())
      {
        pRepo.getConfig().setValue(BRANCH_STRING, pRepoState.getCurrentBranch().getSimpleName(), "pull", "tracking");
      }
      else return dialogResult.isCancel();
    }
    return false;
  }

  private static void _doPush(@NonNull IRepository pRepo, Boolean pIsPushTags, String pRemoteName, @NonNull INotifyUtil pNotifyUtil) throws AditoGitException
  {
    Map<String, EPushResult> failedPushResults = pRepo.push(pIsPushTags, pRemoteName);

    if (!failedPushResults.isEmpty())
    {
      StringBuilder infoText = new StringBuilder();
      for (Map.Entry<String, EPushResult> failedResult : failedPushResults.entrySet())
      {
        infoText.append("Push to remote ref ").append(failedResult.getKey()).append(" failed: ");
        if (failedResult.getValue() == EPushResult.REJECTED_NON_FAST_FORWARD)
        {
          infoText.append("Push was rejected, probably due to existing changes on the remote. Update the local repository via pull and try again");
        }
        else
        {
          infoText.append(failedResult.getValue());
        }
      }
      String infoString = infoText.toString();
      RuntimeException pE = new RuntimeException(infoString);
      pNotifyUtil.notify(pE, "Push failed. " + infoString, false);
    }
  }
}
