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
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 11.10.2018
 */
class IgnoreAction extends AbstractTableAction
{

  private final IAsyncProgressFacade progressFacade;
  private Observable<Optional<IRepository>> repository;
  private Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  IgnoreAction(IIconLoader pIconLoader, IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository,
               @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Ignore", _getIsEnabledObservable(pSelectedFilesObservable));
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.IGNORE_ACTION_ICON));
    progressFacade = pProgressFacade;
    selectedFilesObservable = pSelectedFilesObservable;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Ignoring Files", pHandle -> {
      List<File> files = selectedFilesObservable.blockingFirst()
          .orElse(Collections.emptyList())
          .stream()
          .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
          .collect(Collectors.toList());
      repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).ignore(files);
    });
  }

  /**
   * Only enabled if all selected files are not in the index yet, i.e. have status
   * NEW, MODIFY or MISSING
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return pSelectedFilesObservable.map(selectedFiles -> Optional.of(selectedFiles.orElse(Collections.emptyList()).stream()
                                                                         .allMatch(row -> row.getChangeType().equals(EChangeType.NEW)
                                                                             || row.getChangeType().equals(EChangeType.MODIFY)
                                                                             || row.getChangeType().equals(EChangeType.MISSING))));
  }
}
