package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.dialogs.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Action class for showing the commit dialogs and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
class CommitAction extends AbstractTableAction
{

  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  CommitAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
               @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Commit", _getIsEnabledObservable(pSelectedFilesObservable));
    repository = pRepository;
    dialogProvider = pDialogProvider;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    DialogResult<?, CommitDialogResult> dialogResult = dialogProvider.showCommitDialog(repository, selectedFilesObservable);
    // if user didn't cancel the dialogs
    if (dialogResult.isPressedOk())
    {
      try
      {
        List<File> files = dialogResult.getInformation().getSelectedFilesSupplier().get()
            .stream()
            .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
            .collect(Collectors.toList());
        repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"))
            .commit(dialogResult.getMessage(), files, dialogResult.getInformation().isDoAmend());
      }
      catch (Exception e1)
      {
        throw new RuntimeException(e1);
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return Observable.just(Optional.of(true));
  }
}
