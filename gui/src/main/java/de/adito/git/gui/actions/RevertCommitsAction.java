package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
  private final MergeConflictSequence mergeConflictSequence;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  public RevertCommitsAction(IPrefStore pPrefStore, IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade,
                             MergeConflictSequence pMergeConflictSequence, @Assisted Observable<Optional<IRepository>> pRepository,
                             @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super("Revert Commit(s)", _getIsEnabledObservable(pRepository, pSelectedCommitObservable));
    prefStore = pPrefStore;
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    mergeConflictSequence = pMergeConflictSequence;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));
    progressFacade.executeInBackgroundWithoutIndexing(String.format("Reverting %s commits", selectedCommits.size()), pHandle -> {
      try
      {
        GitIndexLockUtil.checkAndHandleLockedIndexFile(repo, dialogProvider, notifyUtil);
        if (!repo.getStatus().blockingFirst().map(pStatus -> pStatus.getUncommitted().isEmpty()).orElse(true)
            && !ActionUtility.handleStash(prefStore, dialogProvider, repo, STASH_ID_KEY, pHandle))
        {
          return;
        }
        repo.setUpdateFlag(false);
        repo.revertCommit(selectedCommits);
        notifyUtil.notify("Revert commits success", String
            .format("Reverting commits with ID %s was successfull ", selectedCommits.stream()
                .map(ICommit::getId)
                .collect(Collectors.joining(", "))), false);
      }
      finally
      {
        repo.setUpdateFlag(true);
        _unStashChanges(pHandle);
      }
    });
  }

  private void _unStashChanges(@NonNull IProgressHandle pHandle)
  {
    String stashedCommitId = prefStore.get(STASH_ID_KEY);
    if (stashedCommitId != null)
    {
      pHandle.setDescription("Un-stashing changes");
      StashCommand.doUnStashing(mergeConflictSequence, stashedCommitId, Observable.just(repository.blockingFirst()));
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
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepository,
                                                                       @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    Observable<Optional<IRepositoryState>> repoState = pRepository.switchMap(pRepoOpt -> pRepoOpt.map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(pSelectedCommitObservable, repoState, (pSelectedCommitOpt, pRepoStateOpt)
        -> Optional.of(!pSelectedCommitOpt.orElse(Collections.emptyList()).isEmpty() && pRepoStateOpt.map(IRepositoryState::canCommit).orElse(false)));
  }
}
