package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.results.IFileSelectionDialogResult;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 04.10.2019
 */
public class CreatePatchAction extends AbstractTableAction
{

  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repositoryObservable;
  private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  public CreatePatchAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepositoryObservable,
                           @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Create Patch");
    dialogProvider = pDialogProvider;
    repositoryObservable = pRepositoryObservable;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    IFileSelectionDialogResult<?, Object> dialogResult =
        dialogProvider.showNewFileDialog("Choose destination for patch", FileChooserProvider.FileSelectionMode.DIRECTORIRES_ONLY, null, "changes.patch");
    List<File> selectedFiles = selectedFilesObservable.blockingFirst().orElse(List.of()).stream().map(IFileChangeType::getFile).collect(Collectors.toList());
    if (dialogResult.acceptFiles())
    {
      String filePath = dialogResult.getMessage();
      if (!filePath.endsWith(".patch"))
      {
        filePath = filePath + ".patch";
      }
      try (OutputStream outputStream = Files.newOutputStream(Paths.get(filePath)))
      {
        repositoryObservable.blockingFirst().ifPresent(pRepo -> pRepo.createPatch(selectedFiles, null, outputStream));
      }
      catch (IOException pE)
      {
        throw new RuntimeException(pE);
      }
    }
  }
}
