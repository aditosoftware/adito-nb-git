package de.adito.git.gui.dialogs;

import com.google.inject.*;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import io.reactivex.Observable;

import java.util.*;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class DialogProviderImpl implements IDialogProvider
{
  private final IDialogDisplayer dialogDisplayer;
  private final IDialogFactory dialogFactory;

  @Inject
  public DialogProviderImpl(IDialogDisplayer pDialogDisplayer, IDialogFactory pDialogFactory)
  {
    dialogDisplayer = pDialogDisplayer;
    dialogFactory = pDialogFactory;
  }

  @Override
  public DialogResult showMergeConflictDialog(Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs)
  {
    MergeConflictDialog mergeConflictDialog = dialogFactory.create(pRepository, pMergeConflictDiffs);
    boolean pressedOk = dialogDisplayer.showDialog(mergeConflictDialog, "Merge Conflicts", true);
    mergeConflictDialog.discard();
    return new DialogResult(pressedOk, null);
  }

  @Override
  public DialogResult showMergeConflictResolutionDialog(IMergeDiff pMergeDiff)
  {
    boolean pressedOk = dialogDisplayer.showDialog(dialogFactory.create(pMergeDiff),
                                                   "Conflict resolution for file "
                                                       + pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW),
                                                   true);
    return new DialogResult(pressedOk, null);
  }

  @Override
  public DialogResult showDiffDialog(List<IFileDiff> pFileDiffs)
  {
    boolean pressedOk = dialogDisplayer.showDialog(dialogFactory.createDiffDialog(pFileDiffs), "DiffDialog", true);
    return new DialogResult(pressedOk, null);
  }

  @Override
  public DialogResult<CommitDialogResult> showCommitDialog(Observable<Optional<IRepository>> pRepository,
                                                           Observable<Optional<List<IFileChangeType>>> pFilesToCommit)
  {
    String commitMessage = null;
    CommitDialog commitDialog = dialogFactory.createCommitDialog(dialogDisplayer::enableOKButton,
                                                                 dialogDisplayer::disableOKButton,
                                                                 pRepository,
                                                                 pFilesToCommit);
    boolean pressedOk = dialogDisplayer.showDialog(commitDialog, "Commit", false);
    if (pressedOk)
    {
      commitMessage = commitDialog.getMessageText();
    }
    return new DialogResult<>(pressedOk, commitMessage, new CommitDialogResult(commitDialog.getFilesToCommit(), commitDialog.isAmend()));
  }

  @Override
  public DialogResult showNewBranchDialog(Observable<Optional<IRepository>> pRepository)
  {
    NewBranchDialog dialog = dialogFactory.createNewBranchDialog(pRepository, dialogDisplayer::enableOKButton, dialogDisplayer::disableOKButton);
    boolean pressedOk = dialogDisplayer.showDialog(dialog, "New Branch", false);
    return new DialogResult(pressedOk, dialog.getBranchName());
  }

  @Override
  public DialogResult<EResetType> showResetDialog()
  {
    ResetDialog resetDialog = dialogFactory.createResetDialog();
    boolean pressedOk = dialogDisplayer.showDialog(resetDialog, "Reset", true);
    if (pressedOk)
    {
      return new DialogResult<>(true, null, resetDialog.getResetType());
    }
    return new DialogResult<>(false, null);
  }
}
