package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.results.IFileSelectionDialogResult;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
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
        dialogProvider.showFileSelectionDialog("Choose destination for patch", "Selected file", FileChooserProvider.FileSelectionMode.FILES_AND_DIRECTORIES, null);
    List<@NotNull File> selectedFiles = selectedFilesObservable.blockingFirst().orElse(List.of()).stream().map(IFileChangeType::getFile).collect(Collectors.toList());
    if (dialogResult.acceptFiles())
    {
      try (OutputStream outputStream = Files.newOutputStream(Paths.get(dialogResult.getMessage())))
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
