package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
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
  private final IDialogProvider dialogProvider;
  private Observable<Optional<IBranch>> targetBranch;

  @Inject
  MergeAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
              @Assisted Observable<Optional<IBranch>> pTargetBranch)
  {
    super("Merge into Current", _getIsEnabledObservable(pTargetBranch));
    this.dialogProvider = pDialogProvider;
    repositoryObservable = pRepository;
    targetBranch = pTargetBranch;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IRepository repository = repositoryObservable.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
    try
    {
      if (repository.getStatus().blockingFirst().map(IFileStatus::hasUncommittedChanges).orElse(false))
      {
        throw new RuntimeException("Un-committed files detected while trying to merge: Implement stashing or commit/undo changes");
      }
      Optional<IBranch> selectedBranch = targetBranch.blockingFirst();
      if (!selectedBranch.isPresent())
      {
        throw new RuntimeException();
      }
      List<IMergeDiff> mergeConflictDiffs = repository.merge(repository.getCurrentBranch().blockingFirst().orElseThrow(), selectedBranch.get());
      if (!mergeConflictDiffs.isEmpty())
      {
        dialogProvider.showMergeConflictDialog(repositoryObservable, mergeConflictDiffs);
      }
    }
    catch (Exception e1)
    {
      throw new RuntimeException(e1);
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IBranch>> pTargetBranch)
  {
    return pTargetBranch.map(pBranch -> Optional.of(pBranch.isPresent()));
  }
}
