package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
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

  @Inject
  RevertWorkDirAction(IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository,
                      @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Revert", _getIsEnabledObservable(pSelectedFilesObservable));
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.REVERT_ACTION_ICON));
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Reverting", pHandle -> {
      repository.blockingFirst()
          .orElseThrow(() -> new RuntimeException("no valid repository found"))
          .revertWorkDir(selectedFilesObservable.blockingFirst()
                             .orElse(Collections.emptyList())
                             .stream()
                             .map(IFileChangeType::getFile)
                             .collect(Collectors.toList()));
    });
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return pSelectedFilesObservable.map(selectedFiles -> Optional.of(selectedFiles
                                                                         .orElse(Collections.emptyList())
                                                                         .stream()
                                                                         .noneMatch(fileChangeType -> fileChangeType.getChangeType()
                                                                             .equals(EChangeType.SAME))));
  }
}
