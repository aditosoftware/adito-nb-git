package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.*;

/**
 * Action that shows the differences between the selected commit (the list in the observable should contain only one commit for the action to be
 * active) and HEAD. If the optional of pSelectedFile is not empty, the selected file is chosen as active selection in the DiffDialog if more than
 * one file differs in their content from HEAD
 *
 * @author m.kaspera, 13.02.2019
 */
class DiffCommitToHeadAction extends AbstractTableAction
{

  private final Logger logger = Logger.getLogger(DiffCommitToHeadAction.class.getName());
  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final Observable<Optional<String>> selectedFile;

  @Inject
  DiffCommitToHeadAction(IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade, INotifyUtil pNotifyUtil,
                         @Assisted Observable<Optional<IRepository>> pRepository,
                         @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                         @Assisted @Nullable Observable<Optional<String>> pSelectedFile)
  {
    super("Compare with HEAD", _getIsEnabledObservable(pSelectedCommitObservable));
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    notifyUtil = pNotifyUtil;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
    selectedFile = pSelectedFile;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    repository.blockingFirst().ifPresentOrElse(pRepo -> progressFacade.executeInBackground("Creating Diff", pHandle -> {
      List<ICommit> commitList = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
      if (!commitList.isEmpty())
      {
        ICommit selectedCommit = commitList.get(0);
        List<IFileDiff> fileDiffs;
        try
        {
          fileDiffs = pRepo.diff(pRepo.getCommit(null), selectedCommit);
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
        if (!fileDiffs.isEmpty())
          dialogProvider.showDiffDialog(pRepo.getTopLevelDirectory(), fileDiffs, selectedFile.blockingFirst().orElse(null), false, true);
        else
        {
          notifyUtil.notify("No differences found", "No differences found for HEAD and commit " + selectedCommit.getId(), false);
        }
      }
    }), () -> logger.log(Level.SEVERE, () -> "Git: no valid repository found in DiffCommitToHeadAction.actionPerformed"));
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(pICommits -> pICommits.map(pCommitList -> pCommitList.size() == 1));
  }
}
