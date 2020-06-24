package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import de.adito.git.gui.dialogs.results.IStashedCommitSelectionDialogResult;
import de.adito.git.gui.sequences.MergeConflictSequence;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 12.02.2019
 */
class UnStashChangesAction extends AbstractAction
{

  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final INotifyUtil notifyUtil;
  private final MergeConflictSequence mergeConflictSequence;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  UnStashChangesAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil, MergeConflictSequence pMergeConflictSequence,
                       @Assisted Observable<Optional<IRepository>> pRepository)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    mergeConflictSequence = pMergeConflictSequence;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IRepository repo = repository.blockingFirst().orElse(null);
    if (repo != null)
    {
      try
      {
        IStashedCommitSelectionDialogResult<?, String> dialogResult = dialogProvider.showStashedCommitSelectionDialog(Observable.just(Optional.of(repo)),
                                                                                                                      repo.getStashedCommits());
        if (dialogResult.doUnStash())
        {
          _executeUnstash(repo, (DialogResult<?, String>) dialogResult);
        }
      }
      catch (AditoGitException pE)
      {
        notifyUtil.notify(pE, "An error occurred while trying to unstash. ", false);
      }
    }
  }

  /**
   * perform the actual unstash
   *
   * @param pRepo         Repository that is affected by the unstash
   * @param pDialogResult DialogResult with the information the user entered when asked which stashed commit to unstash
   */
  private void _executeUnstash(IRepository pRepo, DialogResult<?, String> pDialogResult)
  {
    progressFacade.executeInBackground("unStashing changes", pHandle -> {
      List<IMergeData> stashConflicts = pRepo.unStashChanges(pDialogResult.getInformation());
      if (!stashConflicts.isEmpty())
      {
        IMergeConflictDialogResult conflictResult = mergeConflictSequence.performMergeConflictSequence(Observable.just(Optional.of(pRepo)),
                                                                                                       stashConflicts, false, "Stash Conflicts");
        if (conflictResult.isFinishMerge())
          dialogProvider.showCommitDialog(Observable.just(Optional.of(pRepo)), Observable.just(Optional.of(List.of())), "");
      }
    });
  }
}
