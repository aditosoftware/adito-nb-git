package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IResetDialogResult;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action that resets the current Branch to the selected commit
 *
 * @author m.kaspera 05.11.2018
 */
class ResetAction extends AbstractTableAction
{


  private final INotifyUtil notifyUtil;
  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  ResetAction(INotifyUtil pNotifyUtil, IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade,
              @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super(Util.getResource(ResetAction.class, "resetTitle"), _getIsEnabledObservable(pSelectedCommitObservable, pRepository));
    notifyUtil = pNotifyUtil;
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IResetDialogResult<?, EResetType> dialogResult = dialogProvider.showResetDialog();
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    if (selectedCommits.size() == 1 && dialogResult.isPerformReset())
    {
      progressFacade.executeAndBlockWithProgress(MessageFormat.format(Util.getResource(ResetAction.class, "resetProgressMsg"), selectedCommits.get(0).getId()),
                                                 pHandle -> {
                                                   _performReset(dialogResult, selectedCommits);
                                                 });
    }
  }

  /**
   * @param pDialogResult    dialogResult with information about which kind of reset should be performed
   * @param pSelectedCommits list with selected commits, the branch is reset to the first commit in the list. Should ideally only contain a single item
   * @throws AditoGitException If the reset could not be performed
   */
  private void _performReset(IResetDialogResult<?, EResetType> pDialogResult, List<ICommit> pSelectedCommits) throws AditoGitException
  {
    IRepository pRepo = repository.blockingFirst()
        .orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));
    try
    {
      GitIndexLockUtil.checkAndHandleLockedIndexFile(pRepo, dialogProvider, notifyUtil);

      pRepo.setUpdateFlag(false);
      pRepo.reset(pSelectedCommits.get(0).getId(), pDialogResult.getInformation());
      notifyUtil.notify(Util.getResource(ResetAction.class, "resetSuccessTitle"),
                        MessageFormat.format(Util.getResource(ResetAction.class, "resetSuccessMsg"), pSelectedCommits.get(0).getId(), pDialogResult.getInformation()),
                        false);
    }
    finally
    {
      pRepo.setUpdateFlag(true);
    }
  }

  /**
   * Checks if exactly one commit is selected and the repository is in a state that allows a reset of the HEAD
   *
   * @param pSelectedCommitObservable Observable with the currently selected commit
   * @param pRepository               Observable of the repository
   * @return Observable that signifies if the action is enabled or disabled
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(@NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                                                       @NonNull Observable<Optional<IRepository>> pRepository)
  {
    Observable<Optional<IRepositoryState>> repoState = pRepository.switchMap(pRepoOpt -> pRepoOpt.map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(pSelectedCommitObservable, repoState, (pSelectedCommitOpt, pRepoStateOpt)
        -> Optional.of(pSelectedCommitOpt.orElse(Collections.emptyList()).size() == 1 && pRepoStateOpt.map(IRepositoryState::canResetHead).orElse(false)));
  }

}
