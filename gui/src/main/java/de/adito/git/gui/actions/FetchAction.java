package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ITrackingRefUpdate;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.AuthCancelledException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.impl.Util;
import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;

import java.awt.event.ActionEvent;
import java.net.UnknownHostException;
import java.text.MessageFormat;
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
    super(Util.getResource(FetchAction.class, "fetchTitle"), _getIsEnabledObservable(pRepository));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeAndBlockWithProgress(Util.getResource(FetchAction.class, "fetchProgressMsg"), pHandle -> {
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
        notifyUtil.notify(Util.getResource(FetchAction.class, "fetchProgressMsg"),
                          Util.getResource(FetchAction.class, "fetchSuccessMsg"), true);
      else
      {
        notifyUtil.notify(Util.getResource(FetchAction.class, "fetchFailedUpdatesTitle"),
                          MessageFormat.format(Util.getResource(FetchAction.class, "fetchFailedUpdatesMsg"), failedUpdates.get(0)), false);
        logger.log(Level.WARNING, () -> Util.getResource(FetchAction.class, "fetchFailedUpdatesLog") + "\n" + StringUtils.join(failedUpdates, "\n"));
      }
    }
    catch (AuthCancelledException pE)
    {
      notifyUtil.notify(Util.getResource(FetchAction.class, "fetchAbortTitle"),
                        Util.getResource(FetchAction.class, "fetchAbortMessage"), false);
      logger.log(Level.WARNING, pE, () -> Util.getResource(FetchAction.class, "fetchAbortLog") + " ");
    }
    catch (AditoGitException pE)
    {
      Throwable rootCause = de.adito.git.impl.util.Util.getRootCause(pE);
      if (rootCause != null && rootCause.getMessage().startsWith("invalid privatekey"))
      {
        notifyUtil.notify(pE, Util.getResource(FetchAction.class, "fetchPrivateKeyInvalid"), false);
      }
      else if (rootCause instanceof UnknownHostException)
      {
        notifyUtil.notify(pE, MessageFormat.format(Util.getResource(FetchAction.class, "unknownHostMsg"), rootCause.getMessage()), false);
      }
      else
      {
        notifyUtil.notify(pE, Util.getResource(FetchAction.class, "fetchUnspecifiedErrorMsg"), false);
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.map(pRepoOpt -> Optional.of(pRepoOpt.isPresent()));
  }
}
