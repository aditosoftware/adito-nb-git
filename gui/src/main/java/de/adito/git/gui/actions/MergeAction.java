package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.AlreadyUpToDateAditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 24.10.2018
 */
class MergeAction extends AbstractTableAction
{

  private static final String STASH_ID_KEY = "merge::stashCommitId";
  private final ISaveUtil saveUtil;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repositoryObservable;
  private final IPrefStore prefStore;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private Observable<Optional<IBranch>> targetBranch;

  @Inject
  MergeAction(IPrefStore pPrefStore, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, ISaveUtil pSaveUtil, INotifyUtil pNotifyUtil,
              @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<IBranch>> pTargetBranch)
  {
    super("Merge into Current", _getIsEnabledObservable(pTargetBranch));
    prefStore = pPrefStore;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    saveUtil = pSaveUtil;
    notifyUtil = pNotifyUtil;
    repositoryObservable = pRepository;
    targetBranch = pTargetBranch;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IBranch selectedBranch = targetBranch.blockingFirst().orElse(null);
    if (selectedBranch == null)
      return;

    // execute
    progressFacade.executeInBackground("Merging " + selectedBranch.getSimpleName() + " into Current", pHandle -> {
      _doMerge(pHandle, selectedBranch);
    });
  }

  private void _doMerge(IProgressHandle pProgressHandle, IBranch pSelectedBranch) throws AditoGitException
  {
    saveUtil.saveUnsavedFiles();
    IRepository repository = repositoryObservable.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
    try
    {
      if (repository.getStatus().blockingFirst().map(IFileStatus::hasUncommittedChanges).orElse(false)
          && !ActionUtility.handleStash(prefStore, dialogProvider, repository, STASH_ID_KEY, pProgressHandle))
      {
        return;
      }
      pProgressHandle.setDescription("Merging branches");
      List<IMergeData> mergeConflictDiffs = repository.merge(repository.getRepositoryState().blockingFirst().orElseThrow().getCurrentBranch(),
                                                             pSelectedBranch);
      if (!mergeConflictDiffs.isEmpty())
      {
        IMergeConflictDialogResult dialogResult = dialogProvider.showMergeConflictDialog(Observable.just(Optional.of(repository)), mergeConflictDiffs, true);
        if (!dialogResult.isFinishMerge())
        {
          pProgressHandle.setDescription("Aborting merge");
          repository.reset(repository.getRepositoryState().blockingFirst().orElseThrow().getCurrentBranch().getId(), EResetType.HARD);
          // do not execute the "show commit dialog" part after this, the finally block should still be executed even if we return here
          return;
        }
      }
      repository.commit("merged " + pSelectedBranch.getSimpleName() + " into "
                            + repository.getRepositoryState().blockingFirst().map(pState -> pState.getCurrentBranch().getSimpleName())
          .orElse("current Branch"));
    }
    catch (AlreadyUpToDateAditoGitException pE)
    {
      notifyUtil.notify("Already up-to-date", "Branches are already up-to-date/merged", false);
    }
    finally
    {
      pProgressHandle.setDescription("Un-stashing saved uncommitted local changes");
      String stashedCommitId = prefStore.get(STASH_ID_KEY);
      if (stashedCommitId != null)
      {
        try
        {
          StashCommand.doUnStashing(dialogProvider, stashedCommitId, Observable.just(Optional.of(repository)));
        }
        finally
        {
          prefStore.put(STASH_ID_KEY, null);
        }
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IBranch>> pTargetBranch)
  {
    return pTargetBranch.map(pBranch -> Optional.of(pBranch.isPresent()));
  }
}
