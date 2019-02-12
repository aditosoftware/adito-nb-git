package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Action class for showing the commit dialogs and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
class CommitAction extends AbstractTableAction
{

  private final IAsyncProgressFacade progressFacade;
  private final String messageTemplate;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  CommitAction(IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
               @Assisted Observable<Optional<IRepository>> pRepository,
               @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, @Assisted String pMessageTemplate)
  {
    super("Commit");
    messageTemplate = pMessageTemplate;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.COMMIT_ACTION_ICON));
    progressFacade = pProgressFacade;
    repository = pRepository;
    dialogProvider = pDialogProvider;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    Observable<Optional<IRepository>> repo = Observable.just(repository.blockingFirst());
    DialogResult<?, CommitDialogResult> dialogResult = dialogProvider.showCommitDialog(repo, selectedFilesObservable, messageTemplate);
    // if user didn't cancel the dialogs
    if (dialogResult.isPressedOk())
    {
      progressFacade.executeInBackground("Committing Changes", pProgress -> {
        List<File> files = dialogResult.getInformation().getSelectedFilesSupplier().get()
            .stream()
            .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
            .collect(Collectors.toList());
        repo.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"))
            .commit(dialogResult.getMessage(), files, dialogResult.getInformation().isDoAmend());
      });
    }
  }

}
