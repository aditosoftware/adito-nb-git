package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IRevertDialogResult;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 31.10.2018
 */
class RevertWorkDirAction extends AbstractTableAction
{

  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;
  private final IDialogProvider dialogProvider;
  private final ISaveUtil saveUtil;

  @Inject
  RevertWorkDirAction(IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, ISaveUtil pSaveUtil,
                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Revert", _getIsEnabledObservable(pSelectedFilesObservable));
    dialogProvider = pDialogProvider;
    saveUtil = pSaveUtil;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.REVERT_ACTION_ICON));
    putValue(Action.SHORT_DESCRIPTION, "Revert changes");
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    saveUtil.saveUnsavedFiles();
    List<IFileChangeType> filesToRevert = selectedFilesObservable.blockingFirst().orElse(Collections.emptyList());
    IRevertDialogResult<?, ?> result = dialogProvider.showRevertDialog(repository, filesToRevert, repository.blockingFirst()
        .map(IRepository::getTopLevelDirectory)
        .orElse(new File("")));
    if (result.isRevertAccepted())
    {
      progressFacade.executeInBackground("Reverting", pHandle -> {
        repository.blockingFirst()
            .orElseThrow(() -> new RuntimeException("no valid repository found"))
            .revertWorkDir(filesToRevert
                               .stream()
                               .map(IFileChangeType::getFile)
                               .collect(Collectors.toList()));
      });
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return pSelectedFilesObservable.map(selectedFiles -> {
      List<IFileChangeType> changeTypes = selectedFiles.orElse(Collections.emptyList());
      if (changeTypes.isEmpty())
        return Optional.of(false);
      return Optional.of(selectedFiles
                             .orElse(Collections.emptyList())
                             .stream()
                             .noneMatch(fileChangeType -> fileChangeType.getChangeType()
                                 .equals(EChangeType.SAME)));
    });
  }
}
