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
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IPushDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
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

  private static final String FAILURE_HEADER = "Push failed";
  private final Logger logger = Logger.getLogger(PushAction.class.getName());
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;

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
    progressFacade.executeInBackground("Pushing Commits", pHandle -> {
      pHandle.setDescription("Collecting Information");
      repository.blockingFirst().ifPresent(pRepo -> _preparePush(pHandle, pRepo));
    });
  }

  private void _preparePush(@NotNull IProgressHandle pHandle, IRepository pRepo)
  {
    IRepositoryState repoState = pRepo.getRepositoryState().blockingFirst().orElse(null);
    String remoteName = (repoState != null && repoState.getRemotes().size() == 1) ? repoState.getRemotes().get(0) : null;
    if (repoState != null && repoState.getCurrentRemoteTrackedBranch() == null && repoState.getRemotes().size() > 1)
    {
      // need new ArrayList to convert to List<Object>
      IUserPromptDialogResult<?, Object> result = dialogProvider.showComboBoxDialog("Select remote to push branch to", new ArrayList<>(repoState.getRemotes()));
      remoteName = (String) result.getInformation();
    }
    try
    {
      List<ICommit> commitList = pRepo.getUnPushedCommits();
      IPushDialogResult<?, Boolean> dialogResult = dialogProvider.showPushDialog(Observable.just(repository.blockingFirst()),
                                                                                 commitList);
      if (dialogResult.isPush())
      {
        pHandle.setDescription("Pushing");
        _doPush(dialogResult.getInformation(), remoteName);
        if (pRepo.getRepositoryState().blockingFirst().map(pRepoState -> pRepoState.getCurrentRemoteTrackedBranch() == null).orElse(false) && remoteName != null)
        {

          pRepo.getConfig().establishTrackingRelationship(repoState.getCurrentBranch().getSimpleName(), repoState.getCurrentBranch().getName(), remoteName);
        }
        notifyUtil.notify("Push", "Push was successful", true);
      }
    }
    catch (GitTransportFailureException pE)
    {
      if (pE.getCause().getMessage().endsWith("push not permitted"))
      {
        notifyUtil.notify(pE, FAILURE_HEADER + " You may have insufficient rights for pushing to the remote. ", false);
      }
      else
      {
        notifyUtil.notify(pE, FAILURE_HEADER + " An error occurred during transport, check your credentials and rights on the remote. ",
                          false);
      }
      logger.log(Level.SEVERE, pE, () -> "failed to push to remote");
    }
    catch (AditoGitException pE)
    {
      String errorMessage = "Error while finding un-pushed commits";
      notifyUtil.notify(pE, errorMessage + ". ", false);
    }
  }

  private void _doPush(Boolean pIsPushTags, String pRemoteName) throws AditoGitException
  {
    Map<String, EPushResult> failedPushResults = repository
        .blockingFirst()
        .orElseThrow(() -> new RuntimeException("no valid repository found"))
        .push(pIsPushTags, pRemoteName);
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
      notifyUtil.notify(pE, "Push failed. " + infoString, false);
    }
  }
}
