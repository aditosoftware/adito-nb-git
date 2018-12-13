package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * @author m.kaspera 24.10.2018
 */
class MergeAction extends AbstractTableAction
{

  private final Observable<Optional<IRepository>> repositoryObservable;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private Observable<Optional<IBranch>> targetBranch;

  @Inject
  MergeAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
              @Assisted Observable<Optional<IBranch>> pTargetBranch)
  {
    super("Merge into Current", _getIsEnabledObservable(pTargetBranch));
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
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
      IRepository repository = repositoryObservable.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
      if (repository.getStatus().blockingFirst().map(IFileStatus::hasUncommittedChanges).orElse(false))
        throw new RuntimeException("Un-committed files detected while trying to merge: Implement stashing or commit/undo changes"); //todo

      List<IMergeDiff> mergeConflictDiffs = repository.merge(repository.getCurrentBranch().blockingFirst().orElseThrow(), selectedBranch);
      if (!mergeConflictDiffs.isEmpty())
        dialogProvider.showMergeConflictDialog(repositoryObservable, mergeConflictDiffs);
    });
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IBranch>> pTargetBranch)
  {
    return pTargetBranch.map(pBranch -> Optional.of(pBranch.isPresent()));
  }
}
