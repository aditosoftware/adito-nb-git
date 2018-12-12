package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.EPushResult;
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
  private Observable<Optional<IRepository>> repository;
  private INotifyUtil notifyUtil;

  /**
   * @param pRepository The repository to push
   */
  @Inject
  PushAction(INotifyUtil pNotifyUtil, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    notifyUtil = pNotifyUtil;
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
    try
    {
      Map<String, EPushResult> failedPushResults = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).push();
      if (failedPushResults.isEmpty())
      {
        notifyUtil.notify("Push success", "Pushing the local commits to the remote was successful", true);
      }
      else
      {
        StringBuilder infoText = new StringBuilder();
        for (Map.Entry<String, EPushResult> failedResult : failedPushResults.entrySet())
        {
          infoText.append("Push to remote ref ").append(failedResult.getKey()).append(" failed with reason: ").append(failedResult.getValue());
        }
        throw new RuntimeException(infoText.toString());
      }
    }
    catch (Exception e1)
    {
      throw new RuntimeException(e1);
    }
  }
}
