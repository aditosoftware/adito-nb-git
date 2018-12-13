package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * This action class creates a newBranchWindow.
 * Also the action open the createBranch in {@link IRepository}. There is an automatically call for the remote
 *
 * @author A.Arnold 18.10.2018
 */
class NewBranchAction extends AbstractAction
{
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private Observable<Optional<IRepository>> repository;

  /**
   * @param pDialogProvider The Interface to provide functionality of giving an overlying framework
   * @param pRepository     The repository where the new branch should exists
   */
  @Inject
  NewBranchAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
    putValue(Action.NAME, "New Branch");
    putValue(Action.SHORT_DESCRIPTION, "Create a new branch in the repository");
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    DialogResult result = dialogProvider.showNewBranchDialog(repository);
    if (result.isPressedOk())
    {
      progressFacade.executeInBackground("Creating branch " + result.getMessage(), pHandle -> {
        IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
        repo.createBranch(result.getMessage(), true); //todo checkout via dialogs
      });
    }
  }
}
