package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.results.IFileSelectionDialogResult;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

/**
 * @author m.kaspera, 04.10.2019
 */
public class ApplyPatchAction extends AbstractTableAction
{
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repositoryObservable;

  @Inject
  public ApplyPatchAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepositoryObservable)
  {
    super("Apply patch");
    dialogProvider = pDialogProvider;
    repositoryObservable = pRepositoryObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    IFileSelectionDialogResult<?, Object> dialogResult =
        dialogProvider.showFileSelectionDialog("Select file that contains the patch", FileChooserProvider.FileSelectionMode.FILES_ONLY, null);
    if (dialogResult.acceptFiles())
    {
      repositoryObservable.blockingFirst().ifPresent(pRepo -> pRepo.applyPatch(new File(dialogResult.getMessage())));
    }
  }
}
