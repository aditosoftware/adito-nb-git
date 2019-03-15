package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.AditoGitException;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 01.02.2019
 */
class OpenFileAction extends AbstractTableAction
{

  private final INotifyUtil notifyUtil;
  private final IFileSystemUtil fileOpener;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  OpenFileAction(INotifyUtil pNotifyUtil, IFileSystemUtil pFileOpener, @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Open", Observable.just(Optional.of(true)));
    notifyUtil = pNotifyUtil;
    fileOpener = pFileOpener;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    selectedFilesObservable.blockingFirst().ifPresent(pIFileChangeTypes -> {
      for (IFileChangeType fileChangeType : pIFileChangeTypes)
      {
        try
        {
          if (fileChangeType.getFile().exists())
            fileOpener.openFile(fileChangeType.getFile());
          else
            notifyUtil.notify("Open File", "Cannot open a deleted file", true);
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
      }
    });
  }
}
