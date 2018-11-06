package de.adito.git.gui.dialogs;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.IMergeDiff;
import io.reactivex.Observable;

import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IDialogProvider {

    DialogResult showMergeConflictDialog(Observable<IRepository> pRepository, List<IMergeDiff> pMergeConflictDiffs);

    DialogResult showMergeConflictResolutionDialog(IMergeDiff pMergeDiff);

    DialogResult showDiffDialog(List<IFileDiff> fileDiffs);

    DialogResult showCommitDialog(Observable<List<IFileChangeType>> pFilesToCommit);

    DialogResult showNewBranchDialog(Observable<IRepository> pRepository);

    DialogResult<EResetType> showResetDialog();

}
