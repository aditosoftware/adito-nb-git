package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ITrackingRefUpdate;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.AuthCancelledException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.impl.util.Util;
import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 17.03.2019
 */
class FetchAction extends AbstractTableAction
{

  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Logger logger = Logger.getLogger(FetchAction.class.getName());

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
    progressFacade.executeInBackground("Fetching from remote(s)", pHandle -> {
      Optional<IRepository> optionalIRepository = repository.blockingFirst(Optional.empty());
      optionalIRepository.ifPresent(this::_performFetch);
    });
  }

  /**
   * Fetches from the remote repository/remote repositories if there is more than one
   *
   * @param pRepo Repository for which the latest info should be fetched from the remote(s)
   */
  private void _performFetch(IRepository pRepo)
  {
    try
    {
      List<ITrackingRefUpdate> fetchResults = pRepo.fetch();
      List<ITrackingRefUpdate> failedUpdates = fetchResults.stream()
          .filter(pITrackingRefUpdate -> !pITrackingRefUpdate.getResult().isSuccessfull())
          .collect(Collectors.toList());
      if (failedUpdates.isEmpty())
        notifyUtil.notify("Fetching from remote(s)", "Fetch from all defined remotes was successful", true);
      else
      {
        notifyUtil.notify("Fetch succeeded with failed updates", "First failed update: " + failedUpdates.get(0) + ". For all failed updates see IDE Log", false);
        logger.log(Level.WARNING, () -> "Fetch succeeded with failed updates:\n" + StringUtils.join(failedUpdates, "\n"));
      }
    }
    catch (AuthCancelledException pE)
    {
      notifyUtil.notify("Aborted fetch", "Fetch was aborted because authentication was cancelled. If you didn't cancel the authentication check the IDE Log for the" +
          " underlying exception to get more information", false);
      logger.log(Level.WARNING, pE, () -> "Git: Received an auth cancelled exception, underlying cause: ");
    }
    catch (AditoGitException pE)
    {
      Throwable rootCause = Util.getRootCause(pE);
      if (rootCause != null && rootCause.getMessage().startsWith("invalid privatekey"))
      {
        notifyUtil.notify(pE, "Privatekey is invalid, check if it really is a ssh key. ", false);
      }
      else
      {
        notifyUtil.notify(pE, "An error occurred while performing the fetch. ", false);
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.map(pRepoOpt -> Optional.of(pRepoOpt.isPresent()));
  }
}
