package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IRepository;
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
  private final Observable<Optional<String>> selectedFileObservable;
  private File projectFolderPath = null;

  @Inject
  OpenFileStringAction(IFileSystemUtil pFileOpener, @Assisted Observable<Optional<IRepository>> pRepository,
                       @Assisted Observable<Optional<String>> pSelectedFileObservable)
  {
    super("Open", Observable.just(Optional.of(true)));
    fileOpener = pFileOpener;
    selectedFileObservable = pSelectedFileObservable;
    pRepository.blockingFirst().ifPresent(pRepo -> projectFolderPath = pRepo.getTopLevelDirectory());
    if (projectFolderPath == null)
      setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    selectedFileObservable.blockingFirst().ifPresent(pFilePath -> {
      try
      {
        fileOpener.openFile(new File(projectFolderPath, pFilePath));
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
    });
  }
}
