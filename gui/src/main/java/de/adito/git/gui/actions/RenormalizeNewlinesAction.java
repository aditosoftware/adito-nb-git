package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.rxjava3.core.Observable;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 04.02.2021
 */
public class RenormalizeNewlinesAction extends AbstractAction
{

  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  public RenormalizeNewlinesAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    repository = pRepository;
  }

  @Override
  public boolean isEnabled()
  {
    return repository.blockingFirst(Optional.empty())
        .map(pRepo -> pRepo.getStatus().blockingFirst(Optional.empty())
            .map(IFileStatus::isClean)
            .orElse(false))
        .orElse(false);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    IRepository repo = repository.blockingFirst().orElse(null);
    progressFacade.executeAndBlockWithProgress(NbBundle.getMessage(RenormalizeNewlinesAction.class, "renormalizeNewlinesTitle"), pHandle -> {
      try
      {
        if (repo != null)
        {
          repo.renormalizeNewlines();
          dialogProvider.showCommitDialog(repository, repo.getStatus().map(pStatus -> pStatus.map(IFileStatus::getUncommitted)),
                                          NbBundle.getMessage(RenormalizeNewlinesAction.class, "renormalizeNewlinesTitle"));
        }
        else
        {
          notifyUtil.notify(NbBundle.getMessage(RenormalizeNewlinesAction.class, "renormalizeNewlinesTitle"),
                            NbBundle.getMessage(RenormalizeNewlinesAction.class, "renormalizeNewlinesFailureNoRepo"), false);
        }
      }
      catch (AditoGitException pE)
      {
        notifyUtil.notify(pE, NbBundle.getMessage(RenormalizeNewlinesAction.class, "renormalizeNewlinesFailureException"), false);
      }
    });
  }
}
