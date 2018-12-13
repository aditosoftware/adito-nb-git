package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

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
  private final IAsyncProgressFacade progressFacade;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;

  /**
   * @param pRepository The repository to push
   */
  @Inject
  PushAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository)
  {
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
      pHandle.setDescription("Collecting Informations");
      Optional<List<ICommit>> commitList = repository.blockingFirst().map(pRepo -> {
        try
        {
          return pRepo.getUnPushedCommits();
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException("Error while finding un-pushed commits", pE);
        }
      });
      boolean doCommit = dialogProvider.showPushDialog(repository, commitList.orElse(Collections.emptyList())).isPressedOk();
      if (doCommit)
      {
        pHandle.setDescription("Pushing");
        _doPush();
      }
    });
  }

  private void _doPush() throws AditoGitException
  {
    Map<String, EPushResult> failedPushResults = repository
        .blockingFirst()
        .orElseThrow(() -> new RuntimeException("no valid repository found"))
        .push();
    if (!failedPushResults.isEmpty())
    {
      StringBuilder infoText = new StringBuilder();
      for (Map.Entry<String, EPushResult> failedResult : failedPushResults.entrySet())
      {
        infoText.append("Push to remote ref ").append(failedResult.getKey()).append(" failed with reason: ").append(failedResult.getValue());
      }
      throw new RuntimeException(infoText.toString());
    }
  }
}
