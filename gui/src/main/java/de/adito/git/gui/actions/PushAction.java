package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EPushResult;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * pushAction to push all commits to the actual chosen branch.
 * To change the actual branch take the checkout method.
 *
 * @author A.Arnold 11.10.2018
 */
class PushAction extends AbstractAction
{
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
      DialogResult<?, Object> result = dialogProvider.showComboBoxDialog("Select remote to push branch to", new ArrayList<>(repoState.getRemotes()));
      remoteName = (String) result.getInformation();
    }
    try
    {
      List<ICommit> commitList = pRepo.getUnPushedCommits();
      DialogResult<?, Boolean> dialogResult = dialogProvider.showPushDialog(Observable.just(repository.blockingFirst()),
                                                                            commitList);
      boolean doCommit = dialogResult.isPressedOk();
      if (doCommit)
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
    catch (AditoGitException pE)
    {
      throw new RuntimeException("Error while finding un-pushed commits", pE);
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
      throw new RuntimeException(infoText.toString());
    }
  }
}
