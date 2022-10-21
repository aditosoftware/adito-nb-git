package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.exception.AlreadyUpToDateAditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.EButtons;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.impl.Util;
import de.adito.git.impl.data.MergeDetailsImpl;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
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
  private final MergeConflictSequence mergeConflictSequence;
  final Observable<Optional<IRepository>> repositoryObservable;
  private final IPrefStore prefStore;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IBranch>> targetBranch;

  @Inject
  MergeAction(IPrefStore pPrefStore, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, ISaveUtil pSaveUtil, INotifyUtil pNotifyUtil,
              MergeConflictSequence pMergeConflictSequence, @Assisted Observable<Optional<IRepository>> pRepoObs, @Assisted Observable<Optional<IBranch>> pTargetBranch)
  {
    super("Merge into Current", _getIsEnabledObservable(pTargetBranch));
    prefStore = pPrefStore;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    saveUtil = pSaveUtil;
    notifyUtil = pNotifyUtil;
    mergeConflictSequence = pMergeConflictSequence;
    repositoryObservable = pRepoObs;
    targetBranch = pTargetBranch;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IBranch selectedBranch = targetBranch.blockingFirst().orElse(null);
    if (selectedBranch == null)
      return;
    IRepository repository = repositoryObservable.blockingFirst().orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));

    GitIndexLockUtil.checkAndHandleLockedIndexFile(repository, dialogProvider, notifyUtil);
    // execute
    progressFacade.executeAndBlockWithProgressWithoutIndexing(MessageFormat.format(Util.getResource(MergeAction.class, "mergeProgressMsg"), selectedBranch.getSimpleName(),
                                                                                   repository.getRepositoryState().blockingFirst(Optional.empty())
                                                                                       .map(pRepositoryState -> pRepositoryState.getCurrentBranch().getSimpleName()).orElse("Current")),
                                                              pHandle -> {
                                                                _doMerge(pHandle, repository, selectedBranch);
                                                              });
  }

  private void _doMerge(IProgressHandle pProgressHandle, IRepository pRepository, IBranch pSelectedBranch) throws AditoGitException
  {
    saveUtil.saveUnsavedFiles();
    boolean unstashChanges = true;
    try
    {
      if (pRepository.getStatus().blockingFirst().map(IFileStatus::hasUncommittedChanges).orElse(false)
          && !ActionUtility.handleStash(prefStore, dialogProvider, pRepository, STASH_ID_KEY, pProgressHandle))
      {
        return;
      }
      IBranch currentBranch = pRepository.getRepositoryState().blockingFirst().orElseThrow().getCurrentBranch();
      pProgressHandle.setDescription("Merging branches");
      List<IMergeData> mergeConflictDiffs = pRepository.merge(currentBranch, pSelectedBranch);
      if (!mergeConflictDiffs.isEmpty())
      {
        IMergeDetails mergeDetails = new MergeDetailsImpl(mergeConflictDiffs, currentBranch.getSimpleName(), pSelectedBranch.getSimpleName());
        IMergeConflictDialogResult<?, ?> dialogResult = mergeConflictSequence.performMergeConflictSequence(Observable.just(Optional.of(pRepository)),
                                                                                                           mergeDetails, true);
        IUserPromptDialogResult<?, ?> promptDialogResult = null;
        if (!(dialogResult.isAbortMerge() || dialogResult.isFinishMerge()))
        {
          promptDialogResult = dialogProvider.showMessageDialog(null, Util.getResource(this.getClass(), "mergeSaveStateQuestion"),
                                                                List.of(EButtons.SAVE, EButtons.ABORT),
                                                                List.of(EButtons.SAVE));
          if (promptDialogResult.isOkay())
          {
            unstashChanges = false;
            notifyUtil.notify("Saved merge state", Util.getResource(this.getClass(), "mergeSavedStateMsg"), false);
            return;
          }
        }
        if (!dialogResult.isFinishMerge() || (promptDialogResult != null && !promptDialogResult.isOkay()))
        {
          pProgressHandle.setDescription("Aborting merge");
          pRepository.reset(currentBranch.getId(), EResetType.HARD);
          // do not execute the "show commit dialog" part after this, the finally block should still be executed even if we return here
          return;
        }
      }
      pRepository.commit("merged " + pSelectedBranch.getSimpleName() + " into " + pRepository.getRepositoryState().blockingFirst()
          .map(pState -> pState.getCurrentBranch().getSimpleName())
          .orElse("current Branch"));
    }
    catch (AlreadyUpToDateAditoGitException pE)
    {
      notifyUtil.notify(Util.getResource(this.getClass(), "mergeBranchesUpToDateTitle"),
                        Util.getResource(this.getClass(), "mergeBranchesUpToDateMsg"), false);
    }
    finally
    {
      _performUnstash(pProgressHandle, pRepository, unstashChanges);
    }
  }

  private void _performUnstash(IProgressHandle pProgressHandle, IRepository pRepository, boolean pUnstashChanges)
  {
    String stashedCommitId = prefStore.get(STASH_ID_KEY);
    if (stashedCommitId != null && pUnstashChanges)
    {
      pProgressHandle.setDescription(Util.getResource(this.getClass(), "unstashChangesMessage"));
      try
      {
        StashCommand.doUnStashing(mergeConflictSequence, stashedCommitId, Observable.just(Optional.of(pRepository)));
      }
      finally
      {
        prefStore.put(STASH_ID_KEY, null);
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IBranch>> pTargetBranch)
  {
    return pTargetBranch.map(pBranch -> Optional.of(pBranch.isPresent()));
  }
}
