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
 * @author m.kaspera, 31.01.2019
 */
class DiffCommitsAction extends AbstractTableAction
{

  private final Logger logger = Logger.getLogger(DiffCommitsAction.class.getName());
  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final Observable<Optional<ICommit>> parentCommitObservable;
  private final Observable<Optional<String>> selectedFile;

  @Inject
  DiffCommitsAction(IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade, INotifyUtil pNotifyUtil,
                    @Assisted Observable<Optional<IRepository>> pRepository,
                    @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                    @Assisted Observable<Optional<ICommit>> pParentCommit,
                    @Assisted @Nullable Observable<Optional<String>> pSelectedFile)
  {
    super("Show Changes", _getIsEnabledObservable(pSelectedCommitObservable, pSelectedFile));
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    notifyUtil = pNotifyUtil;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
    parentCommitObservable = pParentCommit;
    selectedFile = pSelectedFile;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    repository.blockingFirst().ifPresentOrElse(pRepo -> progressFacade.executeInBackground("Creating Diff", pHandle -> {
      _performDiff(pRepo);
    }), () -> logger.log(Level.SEVERE, () -> "Git: no valid repository found in DiffCommitsAction.actionPerformed"));
  }

  /**
   * Performs the diff of the selected commits
   *
   * @param pRepo Repository used for creating the diff, has to be the repository that contains the commits to be diffed
   */
  private void _performDiff(IRepository pRepo)
  {
    List<ICommit> commitList = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    if (!commitList.isEmpty())
    {
      ICommit selectedCommit = commitList.get(0);
      ICommit oldestSelectedCommit = commitList.get(commitList.size() - 1);
      List<IFileDiff> fileDiffs;
      try
      {
        ICommit parentCommit;
        if (oldestSelectedCommit.getParents().isEmpty())
          parentCommit = null;
        else
          parentCommit = oldestSelectedCommit.getParents().get(0);
        fileDiffs = pRepo.diff(selectedCommit, parentCommitObservable.blockingFirst().orElse(parentCommit));
        dialogProvider.showDiffDialog(pRepo.getTopLevelDirectory(), fileDiffs, selectedFile.blockingFirst().orElse(null), false, false);
      }
      catch (AditoGitException pE)
      {
        notifyUtil.notify(pE, "An error occurred while creating the diff. ", false);
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                                                       Observable<Optional<String>> pSelectedFile)
  {
    return Observable.combineLatest(pSelectedCommitObservable, pSelectedFile, (pOptCommits, pFile) -> pOptCommits
        .map(pCommits -> !pCommits.isEmpty() && pFile.isPresent()));
  }
}
