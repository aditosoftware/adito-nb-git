package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action that shows the differences between the selected commit (the list in the observable should contain only one commit for the action to be
 * active) and HEAD. If the optional of pSelectedFile is not empty, the selected file is chosen as active selection in the DiffDialog if more than
 * one file differs in their content from HEAD
 *
 * @author m.kaspera, 13.02.2019
 */
class DiffCommitToHeadAction extends AbstractTableAction
{

  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final Observable<Optional<String>> selectedFile;

  @Inject
  DiffCommitToHeadAction(IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade,
                         @Assisted Observable<Optional<IRepository>> pRepository,
                         @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                         @Assisted @Nullable Observable<Optional<String>> pSelectedFile)
  {
    super("Compare with HEAD", _getIsEnabledObservable(pSelectedCommitObservable));
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
    selectedFile = pSelectedFile;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    progressFacade.executeInBackground("Creating Diff", pHandle -> {
      List<ICommit> commitList = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
      if (!commitList.isEmpty())
      {
        ICommit selectedCommit = commitList.get(0);
        List<IFileDiff> fileDiffs = repository.blockingFirst().map(pRepository -> {
          try
          {
            return pRepository.diff(pRepository.getCommit(null), selectedCommit);
          }
          catch (AditoGitException pE)
          {
            throw new RuntimeException(pE);
          }
        }).orElse(Collections.emptyList());
        dialogProvider.showDiffDialog(fileDiffs, selectedFile.blockingFirst().orElse(null), false, true);
      }
    });
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(pICommits -> pICommits.map(pCommitList -> pCommitList.size() == 1));
  }
}
