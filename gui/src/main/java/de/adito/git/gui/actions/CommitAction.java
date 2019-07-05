package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;
import org.apache.commons.lang3.SystemUtils;

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
  private String messageTemplate;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  CommitAction(IPrefStore pPrefStore, IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
               @Assisted Observable<Optional<IRepository>> pRepository,
               @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, @Assisted String pMessageTemplate)
  {
    super("Commit");
    prefStore = pPrefStore;
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
    Optional<IRepository> currentRepoOpt = repository.blockingFirst();
    String prefStoreInstanceKey = COMMIT_MESSAGE_BASE_STORAGE_KEY + currentRepoOpt.map(pRepo -> pRepo.getTopLevelDirectory().getAbsolutePath()).orElse("");
    boolean doAbort = !currentRepoOpt.map(this::_coverAutoCRLF).orElse(true);
    if (doAbort)
      return;
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

  private boolean _coverAutoCRLF(IRepository pRepository)
  {
    if (SystemUtils.IS_OS_WINDOWS && pRepository.getConfig().getAutoCRLF() == IConfig.AUTO_CRLF.FALSE)
    {
      DialogResult<?, Boolean> dialogResult = dialogProvider
          .showCheckboxPrompt("Your AUTO_CRLF setting is set to FALSE, it is however advised to have it set to TRUE or at least INPUT in order to avoid " +
                                  "introduction of CRLFs into the remote repository.\n Press OK to continue nevertheless", "Set AUTO_CRLF to true for me");
      if (dialogResult.getInformation() && dialogResult.isPressedOk())
      {
        pRepository.getConfig().setAutoCRLF(IConfig.AUTO_CRLF.TRUE);
      }
      return dialogResult.isPressedOk();
    }
    return true;
  }

}
