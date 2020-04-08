package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.progress.IAsyncProgressFacade;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 11.10.2018
 */
class ExcludeAction extends AbstractTableAction
{

  private final IAsyncProgressFacade progressFacade;
  private Observable<Optional<IRepository>> repository;
  private Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  ExcludeAction(IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository,
                @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Exclude", _getIsEnabledObservable(pSelectedFilesObservable));
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Excluding Files", pHandle -> {
      List<File> files = selectedFilesObservable.blockingFirst()
          .orElse(Collections.emptyList())
          .stream()
          .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
          .collect(Collectors.toList());
      repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).exclude(files);
    });
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return pSelectedFilesObservable.map(selectedFiles -> Optional.of(selectedFiles.orElse(Collections.emptyList()).stream()
                                                                         .allMatch(row -> row.getChangeType().equals(EChangeType.NEW))));
  }
}
