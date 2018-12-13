package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.progress.IAsyncProgressFacade;
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

  private final IAsyncProgressFacade progressFacade;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  CommitAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
               @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Commit");
    progressFacade = pProgressFacade;
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
      progressFacade.executeInBackground("Committing Changes", pProgress -> {
        List<File> files = dialogResult.getInformation().getSelectedFilesSupplier().get()
            .stream()
            .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
            .collect(Collectors.toList());
        repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"))
            .commit(dialogResult.getMessage(), files, dialogResult.getInformation().isDoAmend());
      });
    }
  }

}
