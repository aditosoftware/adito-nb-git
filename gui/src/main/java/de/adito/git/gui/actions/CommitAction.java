package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Action class for showing the commit dialogs and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
class CommitAction extends AbstractTableAction
{

  private static final String COMMIT_MESSAGE_BASE_STORAGE_KEY = "adito.git.commit.tmp.message.";

  private final IAsyncProgressFacade progressFacade;
  private final IPrefStore prefStore;
  private final ISaveUtil saveUtil;
  private String messageTemplate;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  CommitAction(IPrefStore pPrefStore, IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, ISaveUtil pSaveUtil,
               @Assisted Observable<Optional<IRepository>> pRepository,
               @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, @Assisted String pMessageTemplate)
  {
    super("Commit", _getIsEnabledObservable(pRepository, pSelectedFilesObservable));
    prefStore = pPrefStore;
    saveUtil = pSaveUtil;
    messageTemplate = pMessageTemplate;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.COMMIT_ACTION_ICON));
    putValue(Action.SHORT_DESCRIPTION, "Commit selected changed files");
    progressFacade = pProgressFacade;
    repository = pRepository;
    dialogProvider = pDialogProvider;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    saveUtil.saveUnsavedFiles();
    Optional<IRepository> currentRepoOpt = repository.blockingFirst();
    String prefStoreInstanceKey = COMMIT_MESSAGE_BASE_STORAGE_KEY + currentRepoOpt.map(pRepo -> pRepo.getTopLevelDirectory().getAbsolutePath()).orElse("");
    Observable<Optional<IRepository>> repo = Observable.just(currentRepoOpt);
    if (messageTemplate == null || messageTemplate.isEmpty())
    {
      messageTemplate = prefStore.get(prefStoreInstanceKey);
      if (messageTemplate == null)
        messageTemplate = "";
    }
    DialogResult<?, CommitDialogResult> dialogResult = dialogProvider.showCommitDialog(repo, selectedFilesObservable, messageTemplate);
    // if user didn't cancel the dialogs
    if (dialogResult.isPressedOk())
    {
      progressFacade.executeInBackground("Committing Changes", pProgress -> {
        List<File> files = dialogResult.getInformation().getSelectedFilesSupplier().get();
        repo.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"))
            .commit(dialogResult.getMessage(), files, dialogResult.getInformation().isDoAmend());
        prefStore.put(prefStoreInstanceKey, null);
      });
    }
    else
    {
      prefStore.put(prefStoreInstanceKey, dialogResult.getMessage());
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                       @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    Observable<Optional<IRepositoryState>> repoState = pRepository.switchMap(pRepoOpt -> pRepoOpt.map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(repoState, pSelectedFilesObservable, (pStateOpt, pStatusOpt)
        -> Optional.of(pStatusOpt.map(pStatus -> !pStatus.isEmpty()).orElse(false) && pStateOpt.map(IRepositoryState::canCommit).orElse(false)));
  }

}
