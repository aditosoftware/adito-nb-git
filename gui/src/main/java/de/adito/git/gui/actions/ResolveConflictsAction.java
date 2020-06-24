package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.AmbiguousStashCommitsException;
import de.adito.git.api.exception.TargetBranchNotFoundException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import de.adito.git.gui.dialogs.results.IStashedCommitSelectionDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import io.reactivex.Observable;

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
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final INotifyUtil notifyUtil;

  @Inject
  public ResolveConflictsAction(IAsyncProgressFacade pProgressFacade, INotifyUtil pNotifyUtil,
                                IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                                @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Resolve Conflicts", _getIsEnabledObservable(pSelectedFilesObservable));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
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
        IUserPromptDialogResult dialogResult = dialogProvider.showYesNoDialog("Could not determine target of operation that led to conflict," +
                                                                       " do you want to go back to the state before merge/pull/cherry pick?");
        if (dialogResult.isOkay())
          pRepo.reset(pTBNFE.getOrigHead().getId(), EResetType.HARD);
      }
      else
      {
        notifyUtil.notify(NOTIFY_MESSAGE, "Could determine neither the target nor the state before the operation that led to the conflict" +
            ", please reset the current branch to the commit you based from manually", false);
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
    IMergeConflictDialogResult<?, ?> mergeConflictDialogResult = dialogProvider.showMergeConflictDialog(repository, conflicts, true, true);
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
