package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.exception.AditoGitException;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.util.*;

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
          notifyUtil.notify(pE, "An error occurred while trying to open the file. ", false);
        }
      }
    });
  }
}
