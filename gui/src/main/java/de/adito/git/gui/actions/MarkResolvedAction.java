package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.progress.IAsyncProgressFacade;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Action that marks the currently selected conflicting files as resolved. Is only active if at least one of the currently selected files has changeType CONFLICTING
 *
 * @author m.kaspera, 26.05.2020
 */
public class MarkResolvedAction extends AbstractTableAction
{

  private static final String NOTIFY_MESSAGE = "Marking conflicting files as resolved";
  private final IAsyncProgressFacade progressFacade;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  public MarkResolvedAction(IAsyncProgressFacade pProgressFacade, INotifyUtil pNotifyUtil, @Assisted Observable<Optional<IRepository>> pRepository,
                            @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Mark resolved", _getIsEnabledObservable(pSelectedFilesObservable));
    progressFacade = pProgressFacade;
    notifyUtil = pNotifyUtil;
    repository = pRepository;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    progressFacade.executeInBackground(NOTIFY_MESSAGE, pHandle -> {
      IRepository repo = repository.blockingFirst().orElseThrow();
      repo.add(selectedFilesObservable.blockingFirst().orElse(List.of()).stream().map(IFileChangeType::getFile).collect(Collectors.toList()));
    });
    notifyUtil.notify("Mark resolved", "Selected conflicting files were marked as resolved", false);
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
