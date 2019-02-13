package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 12.02.2019
 */
public class UnStashChangesAction extends AbstractAction
{

  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  public UnStashChangesAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
                              @Assisted Observable<Optional<IRepository>> pRepository)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IRepository repo = repository.blockingFirst().orElse(null);
    if (repo != null)
    {
      try
      {
        DialogResult<?, String> dialogResult = dialogProvider.showStashedCommitSelectionDialog(Observable.just(Optional.of(repo)),
                                                                                               repo.getStashedCommits());
        if (dialogResult.isPressedOk())
        {
          progressFacade.executeInBackground("unStashing changes", pHandle -> {
            repo.unStashChanges(dialogResult.getInformation());
          });
        }
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
    }
  }
}
