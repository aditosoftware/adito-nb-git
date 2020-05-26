package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.Constants;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * @author m.kaspera, 11.02.2019
 */
class CherryPickAction extends AbstractTableAction
{

  private static final String ACTION_NAME = "Cherry Pick";
  private static final String STASH_ID_KEY = "cherryPick::stashCommitId";
  private final IPrefStore prefStore;
  private final INotifyUtil notifyUtil;
  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final IActionProvider actionProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommits;
  private final ISaveUtil saveUtil;

  @Inject
  CherryPickAction(IPrefStore pPrefStore, IIconLoader pIconLoader, INotifyUtil pNotifyUtil, IDialogProvider pDialogProvider,
                   IAsyncProgressFacade pProgressFacade, ISaveUtil pSaveUtil, IActionProvider pActionProvider,
                   @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<ICommit>>> pSelectedCommits)
  {
    super(ACTION_NAME, _getIsEnabledObservable(pSelectedCommits));
    saveUtil = pSaveUtil;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.CHERRY_PICK));
    putValue(Action.SHORT_DESCRIPTION, "Cherry pick");
    prefStore = pPrefStore;
    notifyUtil = pNotifyUtil;
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    actionProvider = pActionProvider;
    repository = pRepository;
    selectedCommits = pSelectedCommits;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    saveUtil.saveUnsavedFiles();
    IRepository repo = repository.blockingFirst().orElse(null);
    List<ICommit> commitsToPick = selectedCommits.blockingFirst().orElse(Collections.emptyList());
    if (repo != null && !commitsToPick.isEmpty())
    {
      progressFacade.executeInBackground("Cherry picking " + commitsToPick.size() + " commit(s)", pHandle -> {
        _performCherryPick(repo, commitsToPick, pHandle);
      });
    }
  }

  /**
   * Performs the entirety of the cherry pick, including the stashing and unstashing
   *
   * @param pRepo          repository
   * @param pCommitsToPick List of commits that should be cherry picked
   * @param pHandle        ProgressHandle to display the current progress/step
   * @throws AditoGitException thrown if an error occurrs during cherry picking
   */
  private void _performCherryPick(IRepository pRepo, List<ICommit> pCommitsToPick, @NotNull IProgressHandle pHandle) throws AditoGitException
  {
    try
    {
      Optional<IFileStatus> status = pRepo.getStatus().blockingFirst();
      if (status.map(pStatus -> !pStatus.getConflicting().isEmpty()).orElse(true))
      {
        notifyUtil.notify(ACTION_NAME, "Aborting cherry pick, please make sure the working tree is clean before cherry picking", false);
        return;
      }
      if (status.map(pStatus -> !pStatus.getUncommitted().isEmpty()).orElse(false)
          && !ActionUtility.handleStash(prefStore, dialogProvider, pRepo, STASH_ID_KEY, pHandle))
      {
        return;
      }
      pHandle.setDescription("Cherry picking " + pCommitsToPick.size() + " commit(s)");
      _doCherryPick(pRepo, pCommitsToPick);
    }
    finally
    {
      _performUnstash(pHandle);
    }
  }

  /**
   * Performs the unstash operation
   *
   * @param pHandle ProgressHandle to display the current progress/step
   */
  private void _performUnstash(@NotNull IProgressHandle pHandle)
  {
    String stashedCommitId = prefStore.get(STASH_ID_KEY);
    if (stashedCommitId != null)
    {
      pHandle.setDescription("Un-stashing changes");
      StashCommand.doUnStashing(dialogProvider, stashedCommitId, Observable.just(repository.blockingFirst()));
      prefStore.put(STASH_ID_KEY, null);
    }
  }

  private void _doCherryPick(IRepository pRepo, List<ICommit> pCommitsToPick)
  {
    try
    {
      ICherryPickResult cherryPickResult = pRepo.cherryPick(pCommitsToPick);
      if (!cherryPickResult.getConflicts().isEmpty())
      {
        IMergeConflictDialogResult<?, ?> conflictResult = dialogProvider.showMergeConflictDialog(Observable.just(Optional.of(pRepo)),
                                                                                                 cherryPickResult.getConflicts(), true,
                                                                                                 "Cherry Pick conflicts");
        IUserPromptDialogResult<?, ?> promptDialogResult = null;
        if (conflictResult.isFinishMerge())
        {
          Observable<Optional<List<IFileChangeType>>> changedFilesObs = pRepo.getStatus()
              .map(pOptStatus -> pOptStatus.map(IFileStatus::getUncommitted));
          actionProvider.getCommitAction(Observable.just(Optional.of(pRepo)), changedFilesObs, cherryPickResult.getCherryPickHead().getMessage())
              .actionPerformed(null);
        }
        else if (!conflictResult.isAbortMerge())
        {
          promptDialogResult = dialogProvider.showMessageDialog(ResourceBundle.getBundle(getClass().getPackageName() + ".Bundle").getString("mergeSaveStateQuestion"),
                                                                List.of(IDialogDisplayer.EButtons.SAVE, IDialogDisplayer.EButtons.ABORT),
                                                                List.of(IDialogDisplayer.EButtons.SAVE));
          if (promptDialogResult.isOkay())
          {
            notifyUtil.notify("Saved merge state", ResourceBundle.getBundle(getClass().getPackageName() + ".Bundle").getString("mergeSavedStateMessage"), false);
          }
        }
        if (conflictResult.isAbortMerge() || (promptDialogResult != null && !promptDialogResult.isOkay()))
        {
          _abortCherryPick(pRepo);
        }
      }
      else
      {
        notifyUtil.notify(ACTION_NAME, "Cherry pick success", true);
      }
    }
    catch (AditoGitException pE)
    {
      notifyUtil.notify(pE, "Error during Cherry Pick. ", false);
    }
  }

  private void _abortCherryPick(IRepository pRepo) throws AditoGitException
  {
    pRepo.reset(pRepo.getCommit(null).getId(), EResetType.HARD);
    notifyUtil.notify(ACTION_NAME, "Aborting cherry pick by resetting to current HEAD", false);
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(pOptCommits -> pOptCommits.map(pCommits -> !pCommits.isEmpty()));
  }
}
