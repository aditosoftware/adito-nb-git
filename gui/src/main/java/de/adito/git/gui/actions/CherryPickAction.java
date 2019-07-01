package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

  @Inject
  CherryPickAction(IPrefStore pPrefStore, INotifyUtil pNotifyUtil, IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade,
                   IActionProvider pActionProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                   @Assisted Observable<Optional<List<ICommit>>> pSelectedCommits)
  {
    super(ACTION_NAME, _getIsEnabledObservable(pSelectedCommits));
    putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(Constants.CHERRY_PICK)));
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
    IRepository repo = repository.blockingFirst().orElse(null);
    List<ICommit> commitsToPick = selectedCommits.blockingFirst().orElse(Collections.emptyList());
    if (repo != null && !commitsToPick.isEmpty())
    {
      progressFacade.executeInBackground("Cherry picking " + commitsToPick.size() + " commit(s)", pHandle -> {
        try
        {
          Optional<IFileStatus> status = repo.getStatus().blockingFirst();
          if (status.map(pStatus -> !pStatus.getConflicting().isEmpty()).orElse(true))
          {
            notifyUtil.notify(ACTION_NAME, "Aborting cherry pick, please make sure the working tree is clean before cherry picking", false);
            return;
          }
          if (status.map(pStatus -> !pStatus.getUncommitted().isEmpty()).orElse(false))
          {
            if (ActionUtility.isAbortAutostash(prefStore, dialogProvider))
              return;
            pHandle.setDescription("Stashing existing changes");
            prefStore.put(STASH_ID_KEY, repo.stashChanges(null, true));
          }
          pHandle.setDescription("Cherry picking " + commitsToPick.size() + " commit(s)");
          _doCherryPick(repo, commitsToPick);
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
        finally
        {
          String stashedCommitId = prefStore.get(STASH_ID_KEY);
          if (stashedCommitId != null)
          {
            pHandle.setDescription("Un-stashing changes");
            StashCommand.doUnStashing(dialogProvider, stashedCommitId, Observable.just(repository.blockingFirst()));
            prefStore.put(STASH_ID_KEY, null);
          }
        }
      });
    }
  }

  private void _doCherryPick(IRepository pRepo, List<ICommit> pCommitsToPick)
  {
    try
    {
      ICherryPickResult cherryPickResult = pRepo.cherryPick(pCommitsToPick);
      if (!cherryPickResult.getConflicts().isEmpty())
      {
        DialogResult conflictResult = dialogProvider.showMergeConflictDialog(Observable.just(Optional.of(pRepo)), cherryPickResult.getConflicts(), true);
        if (conflictResult.isPressedOk())
        {
          Observable<Optional<List<IFileChangeType>>> changedFilesObs = pRepo.getStatus()
              .map(pOptStatus -> pOptStatus.map(IFileStatus::getUncommitted));
          actionProvider.getCommitAction(Observable.just(Optional.of(pRepo)), changedFilesObs, cherryPickResult.getCherryPickHead().getMessage())
              .actionPerformed(null);
        }
        else
        {
          pRepo.reset(pRepo.getCommit(null).getId(), EResetType.HARD);
          notifyUtil.notify(ACTION_NAME, "Aborting cherry pick by resetting to current HEAD", false);
        }
      }
      else
      {
        notifyUtil.notify(ACTION_NAME, "Cherry pick success", true);
      }
    }
    catch (AditoGitException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(pOptCommits -> pOptCommits.map(pCommits -> !pCommits.isEmpty()));
  }
}
