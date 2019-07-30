package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.impl.util.Util;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 17.03.2019
 */
class FetchAction extends AbstractTableAction
{

  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  FetchAction(INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super("Fetch", _getIsEnabledObservable(pRepository));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Fetching from remote", pHandle -> {
      repository.blockingFirst().ifPresent(pRepo -> {
        try
        {
          pRepo.fetch();
          notifyUtil.notify("Fetching from remote", "Fetch was successful", true);
        }
        catch (AditoGitException pE)
        {
          Throwable rootCause = Util.getRootCause(pE);
          if (rootCause != null && rootCause.getMessage().startsWith("invalid privatekey"))
          {
            notifyUtil.notify("Invalid privatekey", "Privatekey is invalid, check if it really is a ssh key", false);
          }
          else
            throw new RuntimeException(pE);
        }
      });
    });
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.map(pRepoOpt -> Optional.of(pRepoOpt.isPresent()));
  }
}
