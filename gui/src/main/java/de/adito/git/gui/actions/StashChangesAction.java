package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.StashChangesResult;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 12.02.2019
 */
class StashChangesAction extends AbstractAction
{

  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  StashChangesAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil,
                     @Assisted Observable<Optional<IRepository>> pRepository)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IRepository repo = repository.blockingFirst().orElse(null);
    if (repo != null)
    {
      DialogResult<?, StashChangesResult> dialogResult = dialogProvider.showStashChangesDialog();
      if (dialogResult.isPressedOk())
      {
        progressFacade.executeInBackground("Stashing changes", pHandle -> {
          try
          {
            repo.stashChanges(dialogResult.getInformation().getStashMessage(), dialogResult.getInformation().isIncludeUnTracked());
          }
          catch (AditoGitException pE)
          {
            notifyUtil.notify("Error during stash", "An error occurred during the stash process, see the IDE log for further details", false);
            throw new RuntimeException(pE);
          }
        });
      }
    }
  }
}
