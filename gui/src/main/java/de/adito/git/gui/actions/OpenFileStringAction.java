package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.exception.AditoGitException;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

/**
 * Action that opens the file that is formed with the relative path from the Observable Optional String and the root folder of the repository, as
 * gotten from the repo observable
 *
 * @author m.kaspera, 01.02.2019
 */
class OpenFileStringAction extends AbstractTableAction
{

  private final IFileSystemUtil fileOpener;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<String>> selectedFileObservable;

  /**
   * @param pFileOpener             IFileSystemUtil used to open files in the Netbeans editor
   * @param pSelectedFileObservable Observable Optional of the absolute path of the file to be opened
   */
  @Inject
  OpenFileStringAction(IFileSystemUtil pFileOpener, INotifyUtil pNotifyUtil, @Assisted Observable<Optional<String>> pSelectedFileObservable)
  {
    super("Open", _getIsEnabledObservable(pSelectedFileObservable));
    fileOpener = pFileOpener;
    notifyUtil = pNotifyUtil;
    selectedFileObservable = pSelectedFileObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    selectedFileObservable.blockingFirst().ifPresent(pFilePath -> {
      try
      {
        fileOpener.openFile(new File(pFilePath));
      }
      catch (AditoGitException pE)
      {
        notifyUtil.notify(pE, "An error occurred while trying to open the file. ", false);
      }
    });
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<String>> pSelectedFileObservable)
  {
    return pSelectedFileObservable.map(pSelectedFileOpt -> pSelectedFileOpt.map(pFileString -> new File(pFileString).exists()));
  }
}
