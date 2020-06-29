package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.*;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.*;
import de.adito.git.gui.dialogs.panels.NotificationPanel;
import de.adito.git.gui.dialogs.results.*;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.util.*;

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
  private final INotifyUtil notifyUtil;

  @Inject
  public ResolveConflictsAction(IAsyncProgressFacade pProgressFacade, INotifyUtil pNotifyUtil, MergeConflictSequence pMergeConflictSequence,
                                IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                                @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super(Util.getResource(ResolveConflictsAction.class, "resolveConflictsTitle"), _getIsEnabledObservable(pSelectedFilesObservable));
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
    List<IMergeData> conflicts;
    try
    {
      conflicts = pRepo.getConflicts();
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
        conflicts = pRepo.getStashConflicts(selectedStashCommitId);
      }
      else
      {
        return;
      }
    }
    IMergeConflictDialogResult<?, ?> mergeConflictDialogResult = mergeConflictSequence.performMergeConflictSequence(repository, conflicts, true);
    if (mergeConflictDialogResult.isAbortMerge())
    {
      pRepo.reset(pRepo.getRepositoryState().blockingFirst().orElseThrow().getCurrentBranch().getId(), EResetType.HARD);
    }
    else if (mergeConflictDialogResult.isFinishMerge())
    {
      dialogProvider.showCommitDialog(repository, pRepo.getStatus().map(pStatusOpt -> pStatusOpt.map(IFileStatus::getUncommitted)),
                                      "merged xxx into " + pRepo.getRepositoryState().blockingFirst(Optional.empty())
                                          .map(pRepoState -> pRepoState.getCurrentBranch().getSimpleName()).orElse(""));
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
