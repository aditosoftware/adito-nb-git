package de.adito.git.gui.dialogs;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IDialogProvider
{

  DialogResult showMergeConflictDialog(Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs);

  DialogResult showMergeConflictResolutionDialog(IMergeDiff pMergeDiff);

  DialogResult showDiffDialog(List<IFileDiff> pFileDiffs);

  DialogResult<CommitDialog, CommitDialogResult> showCommitDialog(Observable<Optional<IRepository>> pRepository,
                                                                  Observable<Optional<List<IFileChangeType>>> pFilesToCommit);

  DialogResult showNewBranchDialog(Observable<Optional<IRepository>> pRepository);

  DialogResult<ResetDialog, EResetType> showResetDialog();

  DialogResult<PushDialog, Object> showPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  DialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(Observable<Optional<IRepository>> pRepository,
                                                                                      List<ICommit> pStashedCommits);

  DialogResult showPasswordPromptDialog(String pMessage);

  DialogResult showUserPromptDialog(String pMessage);
}
