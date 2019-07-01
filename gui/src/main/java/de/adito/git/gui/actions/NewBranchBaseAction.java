package de.adito.git.gui.actions;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 01.07.2019
 */
abstract class NewBranchBaseAction extends AbstractAction
{

  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;

  /**
   * @param pProgressFacade ProgressFacade which is used to report back the progess of the task
   * @param pDialogProvider The Interface to provide functionality of giving an overlying framework
   */
  @Inject
  NewBranchBaseAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    putValue(Action.NAME, getActionName());
    putValue(Action.SHORT_DESCRIPTION, getShortDescription());
  }

  abstract Observable<Optional<ICommit>> getStartPoint();

  abstract Observable<Optional<IRepository>> getRepository();

  abstract String getActionName();

  abstract String getShortDescription();

  @Override
  public void actionPerformed(ActionEvent e)
  {
    DialogResult<?, Boolean> result = dialogProvider.showNewBranchDialog(getRepository());
    if (result.isPressedOk())
    {
      progressFacade.executeInBackground("Creating branch " + result.getMessage(), pHandle -> {
        IRepository repo = getRepository().blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
        repo.createBranch(result.getMessage(), getStartPoint().blockingFirst().orElse(null), result.getInformation());
      });
    }
  }
}
