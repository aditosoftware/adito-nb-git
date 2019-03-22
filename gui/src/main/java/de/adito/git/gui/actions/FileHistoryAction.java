package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 06.02.2019
 */
class FileHistoryAction extends AbstractTableAction
{

  private final IWindowProvider windowProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<List<File>> filesObservable;

  @Inject
  FileHistoryAction(IIconLoader pIconLoader, IWindowProvider pWindowProvider,
                    @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<List<File>> pFile)
  {
    super("Show file history", _getIsEnabledObservable(pFile));
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.FILE_HISTORY_ACTION_ICON));
    putValue(Action.SHORT_DESCRIPTION, "Show git file history");
    windowProvider = pWindowProvider;
    repository = pRepository;
    filesObservable = pFile;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    windowProvider.showFileCommitHistoryWindow(repository, filesObservable.blockingFirst().get(0));
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<List<File>> pSelectedFileObservable)
  {
    return pSelectedFileObservable.map(selectedFiles -> Optional.of(selectedFiles.size() == 1));
  }
}
