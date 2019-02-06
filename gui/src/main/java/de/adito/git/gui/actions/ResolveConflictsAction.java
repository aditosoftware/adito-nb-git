package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.api.exception.AmbiguousStashCommitsException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 14.12.2018
 */
class ResolveConflictsAction extends AbstractTableAction
{
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  public ResolveConflictsAction(IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
                                @Assisted Observable<Optional<IRepository>> pRepository,
                                @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Resolve Conflicts", _getIsEnabledObservable(pSelectedFilesObservable));
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.RESOLVE_CONFLICTS_ACTION_ICON));
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Conflict resolution", pHandle -> {
      IRepository repo = repository.blockingFirst().orElseThrow();
      List<IMergeDiff> conflicts;
      try
      {
        conflicts = repo.getConflicts();
      }
      catch (AmbiguousStashCommitsException pE)
      {
        DialogResult<?, String> dialogResult = dialogProvider.showStashedCommitSelectionDialog(repository, repo.getStashedCommits());
        if (dialogResult.isPressedOk())
        {
          String selectedStashCommitId = dialogResult.getInformation();
          conflicts = repo.getStashConflicts(selectedStashCommitId);
        }
        else
        {
          return;
        }
      }
      dialogProvider.showMergeConflictDialog(repository, conflicts);
    });
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
