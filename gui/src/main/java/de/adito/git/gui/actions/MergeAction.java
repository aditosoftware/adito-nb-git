package de.adito.git.gui.actions;

import com.google.common.annotations.VisibleForTesting;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 24.10.2018
 */
class MergeAction extends AbstractTableAction
{

  private static final String STASH_ID_KEY = "merge::stashCommitId";
  private final ISaveUtil saveUtil;
  private final INotifyUtil notifyUtil;
  private final IActionProvider actionProvider;
  private final MergeConflictSequence mergeConflictSequence;
  final Observable<Optional<IRepository>> repositoryObservable;
  private final IPrefStore prefStore;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IBranch>> targetBranch;

  @Inject
  MergeAction(IPrefStore pPrefStore, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, ISaveUtil pSaveUtil, INotifyUtil pNotifyUtil,
              IActionProvider pActionProvider, MergeConflictSequence pMergeConflictSequence, @Assisted Observable<Optional<IRepository>> pRepoObs,
              @Assisted Observable<Optional<IBranch>> pTargetBranch)
  {
    super("Merge into Current", _getIsEnabledObservable(pTargetBranch));
    prefStore = pPrefStore;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    saveUtil = pSaveUtil;
    notifyUtil = pNotifyUtil;
    actionProvider = pActionProvider;
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

  /**
   * perform the actual merge
   *
   * @param pProgressHandle ProgressHandle that allows informing the user about the current work
   * @param pRepository     Repository of the project that the merge is done in
   * @param pSelectedBranch The branch that is merged into the current branch
   * @throws AditoGitException if the stash/unstash encounters a problem, an error is encountered during the merge or the result cannot be commited
   */
  private void _doMerge(@NonNull IProgressHandle pProgressHandle, @NonNull IRepository pRepository, @NonNull IBranch pSelectedBranch) throws AditoGitException
  {
    saveUtil.saveUnsavedFiles();
    boolean unstashChanges = true;
    AfterMergeConflictsSteps afterMergeConflictsSteps = AfterMergeConflictsSteps.COMMIT_AND_UNSTASH;
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
      String suggestedCommitMessage = createSuggestedCommitMessage(pRepository, pSelectedBranch, mergeConflictDiffs);
      if (!mergeConflictDiffs.isEmpty())
      {
        IMergeDetails mergeDetails = new MergeDetailsImpl(mergeConflictDiffs, currentBranch.getSimpleName(), pSelectedBranch.getSimpleName());
        afterMergeConflictsSteps = dealWithMergeConflicts(pProgressHandle, pRepository, currentBranch, mergeDetails);
      }
      if (afterMergeConflictsSteps.isCommitWithDialog())
        commitAfterMergeConflicts(pRepository, suggestedCommitMessage);
        // in case there were no conflicts just commit the changes with the default message
      else if (afterMergeConflictsSteps.isCommit)
        pRepository.commit(suggestedCommitMessage);
    }
    catch (AlreadyUpToDateAditoGitException pE)
    {
      notifyUtil.notify(Util.getResource(this.getClass(), "mergeBranchesUpToDateTitle"),
                        Util.getResource(this.getClass(), "mergeBranchesUpToDateMsg"), false);
    }
    finally
    {
      if (afterMergeConflictsSteps.isUnstash())
        _performUnstash(pProgressHandle, pRepository, unstashChanges);
    }
  }

  /**
   * Create a commit message that contains information about the merge:
   * - which branches were mergen
   * - if there were conflicts, list which files had conflicts
   *
   * @param pRepository         Repository of the project that the merge is done in
   * @param pSelectedBranch     The branch that is merged into the current branch
   * @param pMergeConflictDiffs List of IMergeData that contains the conflicting files
   * @return String with the message that should be given to the commit dialog as template for the user
   */
  @VisibleForTesting
  @NonNull
  static String createSuggestedCommitMessage(@NonNull IRepository pRepository, @NonNull IBranch pSelectedBranch, @NonNull List<IMergeData> pMergeConflictDiffs)
  {
    String mergeMessage = "merged " + pSelectedBranch.getSimpleName() + " into " + pRepository.getRepositoryState().blockingFirst()
        .map(pState -> pState.getCurrentBranch().getSimpleName())
        .orElse("current Branch");
    if (pMergeConflictDiffs.isEmpty())
    {
      return mergeMessage;
    }
    else
    {
      String conflictingFiles = pMergeConflictDiffs.stream().map(IMergeData::getFilePath).map(pPath -> "#     " + pPath).collect(Collectors.joining("\n"));
      return String.format("%s\n\n#  merge had %d conflicting files:\n%s", mergeMessage, pMergeConflictDiffs.size(), conflictingFiles);
    }
  }

  /**
   * Perform the mergeConflictsSequence and handle the abort and save of the current merge
   *
   * @param pProgressHandle ProgressHandle that allows informing the user about the current work
   * @param pRepository     Repository of the project that the merge is done in
   * @param pCurrentBranch  Branch that the user is coming from (branch the user was on before he invoked the merge)
   * @param pMergeDetails   Details of the merge, includes conflicting files and such
   * @return AfterMergeConflictsSteps that determine if a commit and unstash should happen
   * @throws AditoGitException thrown if the user tries to abort the merge and HEAD cannot be reset to the tip of the current branch
   */
  @NonNull
  private AfterMergeConflictsSteps dealWithMergeConflicts(@NonNull IProgressHandle pProgressHandle, @NonNull IRepository pRepository, @NonNull IBranch pCurrentBranch,
                                                          @NonNull IMergeDetails pMergeDetails) throws AditoGitException
  {
    IMergeConflictDialogResult<?, ?> dialogResult = mergeConflictSequence.performMergeConflictSequence(Observable.just(Optional.of(pRepository)),
                                                                                                       pMergeDetails, true);
    IUserPromptDialogResult<?, ?> promptDialogResult = null;
    if (!(dialogResult.isAbortMerge() || dialogResult.isFinishMerge()))
    {
      promptDialogResult = dialogProvider.showMessageDialog(null, Util.getResource(this.getClass(), "mergeSaveStateQuestion"),
                                                            List.of(EButtons.SAVE, EButtons.ABORT),
                                                            List.of(EButtons.SAVE));
      if (promptDialogResult.isOkay())
      {
        notifyUtil.notify("Saved merge state", Util.getResource(this.getClass(), "mergeSavedStateMsg"), false);
        return AfterMergeConflictsSteps.NO_COMMIT_NO_UNSTASH;
      }
    }
    if (!dialogResult.isFinishMerge() || (promptDialogResult != null && !promptDialogResult.isOkay()))
    {
      pProgressHandle.setDescription("Aborting merge");
      pRepository.reset(pCurrentBranch.getId(), EResetType.HARD);
      // do not execute the "show commit dialog" part after this (no changes), but unstash the previously changed files
      return AfterMergeConflictsSteps.NO_COMMIT_BUT_UNSTASH;
    }
    // there were conflicting files in the merge, so present the user with the dialog and a preset commit message and let him decide if the message suits him
    return AfterMergeConflictsSteps.COMMIT_WITH_DIALOG_AND_UNSTASH;
  }

  /**
   * Show the commit dialog, and based on the user input commit the selected changes (or leave them as-is if the user aborts)
   *
   * @param pRepository            repository that contains the changes to commit
   * @param suggestedCommitMessage message that should be preset for user convenience
   */
  private void commitAfterMergeConflicts(@NonNull IRepository pRepository, @NonNull String suggestedCommitMessage)
  {
    actionProvider.getCommitAction(Observable.just(Optional.of(pRepository)), pRepository.getStatus()
                                       .map(pStatus -> pStatus.map(IFileStatus::getUncommitted)),
                                   suggestedCommitMessage).actionPerformed(null);
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

  /**
   * Enum that represents steps that should be done after a merge conflict resolution
   */
  @AllArgsConstructor
  private enum AfterMergeConflictsSteps
  {
    /**
     * Do a fast commit (uses default commit message, does not ask the user for the commit message) and unstash the stashed changes afterwards
     */
    COMMIT_AND_UNSTASH(true, false, true),
    /**
     * Do a commit with commit message approval (show the default commit dialog with preset commit message template) from the user and unstash the stashed changes afterwards
     */
    COMMIT_WITH_DIALOG_AND_UNSTASH(false, true, true),
    /**
     * Do not commit but perform an unstash (used in conjunction with an aborted merge -> reset to state before the merge began and unstash changes)
     */
    NO_COMMIT_BUT_UNSTASH(false, false, true),
    /**
     * Neither commit nor unstash, used if some conflicts were resolved and the current state of the merge should be kept for later
     */
    NO_COMMIT_NO_UNSTASH(false, false, false);

    @Getter
    private final boolean isCommit;
    @Getter
    private final boolean isCommitWithDialog;
    @Getter
    private final boolean isUnstash;
  }
}
