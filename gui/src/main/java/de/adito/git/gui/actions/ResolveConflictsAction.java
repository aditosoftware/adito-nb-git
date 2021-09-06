package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.AmbiguousStashCommitsException;
import de.adito.git.api.exception.TargetBranchNotFoundException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.EButtons;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.panels.NotificationPanel;
import de.adito.git.gui.dialogs.results.*;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 14.12.2018
 */
class ResolveConflictsAction extends AbstractTableAction
{
  private static final String NOTIFY_MESSAGE = "Conflict resolution";
  private final IAsyncProgressFacade progressFacade;
  private final MergeConflictSequence mergeConflictSequence;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final IPrefStore prefStore;
  private final INotifyUtil notifyUtil;

  @Inject
  public ResolveConflictsAction(IAsyncProgressFacade pProgressFacade, IPrefStore pPrefStore, INotifyUtil pNotifyUtil, MergeConflictSequence pMergeConflictSequence,
                                IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                                @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super(Util.getResource(ResolveConflictsAction.class, "resolveConflictsTitle"), _getIsEnabledObservable(pSelectedFilesObservable));
    prefStore = pPrefStore;
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    mergeConflictSequence = pMergeConflictSequence;
    dialogProvider = pDialogProvider;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground(NOTIFY_MESSAGE, pHandle -> {
      IRepository repo = repository.blockingFirst().orElseThrow();
      _resolveConflicts(repo);
    });
  }

  private void _resolveConflicts(IRepository pRepo) throws AditoGitException
  {
    IMergeDetails mergeDetails;
    try
    {
      mergeDetails = pRepo.getConflicts();
    }
    catch (TargetBranchNotFoundException pTBNFE)
    {
      if (pTBNFE.getOrigHead() != null)
      {
        NotificationPanel notificationPanel = dialogProvider.getPanelFactory().createNotificationPanel(Util.getResource(ResolveConflictsAction.class, "tbnfeMsg"));
        NotificationPanel detailsPanel = dialogProvider.getPanelFactory().createNotificationPanel(Util.getResource(ResolveConflictsAction.class, "tbnfeDetailsMsg"));
        IUserPromptDialogResult<?, Object> dialogResult = dialogProvider.showDialog(dialogProvider.getPanelFactory().getExpandablePanel(notificationPanel, detailsPanel),
                                                                                    Util.getResource(ResolveConflictsAction.class, "resolveConflictsTitle"),
                                                                                    List.of(EButtons.RESET_HEAD, EButtons.LEAVE_BE),
                                                                                    List.of(EButtons.RESET_HEAD));
        if (dialogResult.isOkay())
          pRepo.reset(pTBNFE.getOrigHead().getId(), EResetType.HARD);
      }
      else
      {
        notifyUtil.notify(NOTIFY_MESSAGE, Util.getResource(ResolveConflictsAction.class, "resolveConflictsFailureMsg"), false);
      }
      return;
    }
    catch (AmbiguousStashCommitsException pE)
    {
      IStashedCommitSelectionDialogResult<?, String> dialogResult = dialogProvider.showStashedCommitSelectionDialog(repository, pRepo.getStashedCommits());
      if (dialogResult.doUnStash())
      {
        String selectedStashCommitId = dialogResult.getInformation();
        mergeDetails = pRepo.getStashConflicts(selectedStashCommitId);
      }
      else
      {
        return;
      }
    }
    IMergeConflictDialogResult<?, ?> mergeConflictDialogResult = mergeConflictSequence.performMergeConflictSequence(repository, mergeDetails, true);
    if (mergeConflictDialogResult.isAbortMerge())
    {
      pRepo.reset(pRepo.getRepositoryState().blockingFirst().orElseThrow().getCurrentBranch().getId(), EResetType.HARD);
    }
    else if (mergeConflictDialogResult.isFinishMerge())
    {
      ICommitDialogResult<?, CommitDialogResult> dialogResult = dialogProvider
          .showCommitDialog(repository, pRepo.getStatus().map(pStatusOpt -> pStatusOpt.map(IFileStatus::getUncommitted)),
                            "merged xxx into " + pRepo.getRepositoryState().blockingFirst(Optional.empty())
                                .map(pRepoState -> pRepoState.getCurrentBranch().getSimpleName()).orElse(""));
      if (dialogResult.doCommit())
        CommitAction.performCommit(repository, progressFacade, prefStore, dialogResult, null);
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    // if one of the selected files has status conflicting
    return pSelectedFilesObservable
        .map(pOptFileChangeTypes -> Optional.of(pOptFileChangeTypes
                                                    .map(pFileChangeTypes -> pFileChangeTypes.stream()
                                                        .anyMatch(pFileChangeType -> pFileChangeType.getChangeType() == EChangeType.CONFLICTING))
                                                    .orElse(false)));
  }
}
