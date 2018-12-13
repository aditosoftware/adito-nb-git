package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 12.10.2018
 */
class DiffToHeadAction extends AbstractTableAction
{

  private final IAsyncProgressFacade progressFacade;
  private Observable<Optional<IRepository>> repository;
  private IDialogProvider dialogProvider;
  private Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

  @Inject
  DiffToHeadAction(IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository,
                   @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    super("Show Diff");
    progressFacade = pProgressFacade;
    repository = pRepository;
    dialogProvider = pDialogProvider;
    selectedFilesObservable = pSelectedFilesObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Creating Diff", pHandle -> {
      List<File> files = selectedFilesObservable.blockingFirst()
          .orElse(Collections.emptyList())
          .stream()
          .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
          .collect(Collectors.toList());
      List<IFileDiff> fileDiffs = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).diff(files, null);

      //Show Dialog in EDT -> Handle gets finished
      SwingUtilities.invokeLater(() -> dialogProvider.showDiffDialog(fileDiffs));
    });
  }

}
