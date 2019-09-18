package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action that reverts the changes that werde done by one or more commits
 *
 * @author m.kaspera, 18.09.2019
 */
public class RevertCommitsAction extends AbstractTableAction
{

  private static final String STASH_ID_KEY = "revertCommits::stashIdKey";
  private final IPrefStore prefStore;
  private final IDialogProvider dialogProvider;
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  public RevertCommitsAction(IPrefStore pPrefStore, IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade,
                             @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super("Revert Commit(s)", _getIsEnabledObservable(pRepository, pSelectedCommitObservable));
    prefStore = pPrefStore;
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
    if (!repo.getStatus().blockingFirst().map(pStatus -> pStatus.getUncommitted().isEmpty()).orElse(true))
      progressFacade.executeInBackground(String.format("Reverting %s commits", selectedCommits.size()), pHandle -> {
        try
        {
          if (_stashChanges(selectedCommits, repo, pHandle)) return;
        }
        finally
        {
          _unStashChanges(pHandle);
        }
      });
  }

  private boolean _stashChanges(List<ICommit> pSelectedCommits, IRepository pRepo, @NotNull IProgressHandle pHandle) throws AditoGitException
  {
    if (ActionUtility.isAbortAutostash(prefStore, dialogProvider))
      return true;
    pHandle.setDescription("Stashing existing changes");
    prefStore.put(STASH_ID_KEY, pRepo.stashChanges(null, true));
    pRepo.revertCommis(pSelectedCommits);
    notifyUtil.notify("Revert commits success", String.format("Reverting commits with ID %s was successfull ", pSelectedCommits), false);
    return false;
  }

  private void _unStashChanges(@NotNull IProgressHandle pHandle)
  {
    String stashedCommitId = prefStore.get(STASH_ID_KEY);
    if (stashedCommitId != null)
    {
      pHandle.setDescription("Un-stashing changes");
      StashCommand.doUnStashing(dialogProvider, stashedCommitId, Observable.just(repository.blockingFirst()));
      prefStore.put(STASH_ID_KEY, null);
    }
  }

  /**
   * Checks if any commit is selected and the repository is in a state that allows committing
   *
   * @param pRepository               Observable of the repository
   * @param pSelectedCommitObservable Observable with the currently selected commit
   * @return Observable that signifies if the action is enabled or disabled
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                       @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    Observable<Optional<IRepositoryState>> repoState = pRepository.switchMap(pRepoOpt -> pRepoOpt.map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(pSelectedCommitObservable, repoState, (pSelectedCommitOpt, pRepoStateOpt)
        -> Optional.of(!pSelectedCommitOpt.orElse(Collections.emptyList()).isEmpty() && pRepoStateOpt.map(IRepositoryState::canCommit).orElse(false)));
  }
}
