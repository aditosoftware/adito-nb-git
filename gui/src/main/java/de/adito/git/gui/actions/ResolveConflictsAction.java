package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AmbiguousStashCommitsException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * @author m.kaspera, 14.12.2018
 */
public class ResolveConflictsAction extends AbstractTableAction
{
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  public ResolveConflictsAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
                                @Assisted Observable<Optional<IRepository>> pRepository,
                                @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Resolve Conflicts", _getIsEnabledObservable(pSelectedFilesObservable));
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
