package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDialogDisplayer;
import io.reactivex.Observable;

import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class DialogProviderImpl implements IDialogProvider
{
    private final IDialogDisplayer dialogDisplayer;
    private final IDialogFactory dialogFactory;

    @Inject
    public DialogProviderImpl(IDialogDisplayer pDialogDisplayer, IDialogFactory pDialogFactory) {
        dialogDisplayer = pDialogDisplayer;
        dialogFactory = pDialogFactory;
    }

    @Override
    public DialogResult showMergeConflictDialog(Observable<IRepository> pRepository, List<IMergeDiff> pMergeConflictDiffs) {
        boolean pressedOk = dialogDisplayer.showDialog(dialogFactory.create(pRepository, pMergeConflictDiffs), "Merge Conflicts", true);
        return new DialogResult(pressedOk, null);
    }

    @Override
    public DialogResult showMergeConflictResolutionDialog(IMergeDiff pMergeDiff) {
        boolean pressedOk = dialogDisplayer.showDialog(dialogFactory.create(pMergeDiff), "Conflict resolution for file " + pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW), true);
        return new DialogResult(pressedOk, null);
    }

    @Override
    public DialogResult showDiffDialog(List<IFileDiff> fileDiffs) {
        boolean pressedOk = dialogDisplayer.showDialog(dialogFactory.createDiffDialog(fileDiffs), "DiffDialog", true);
        return new DialogResult(pressedOk, null);
    }

    @Override
    public DialogResult showCommitDialog(Observable<List<IFileChangeType>> pFilesToCommit) {
        String commitMessage = null;
        CommitDialog commitDialog = dialogFactory.createCommitDialog(dialogDisplayer::enableOKButton, dialogDisplayer::disableOKButton, pFilesToCommit);
        boolean pressedOk = dialogDisplayer.showDialog(commitDialog, "Commit", false);
        if(pressedOk) {
            commitMessage = commitDialog.getMessageText();
        }
        return new DialogResult(pressedOk, commitMessage);
    }

    @Override
    public DialogResult showNewBranchDialog(Observable<IRepository> pRepository) {
        NewBranchDialog dialog = dialogFactory.createNewBranchDialog(pRepository, dialogDisplayer::enableOKButton, dialogDisplayer::disableOKButton);
        boolean pressedOk = dialogDisplayer.showDialog(dialog, "New Branch", false);
        return new DialogResult(pressedOk, dialog.getBranchName());
    }

    @Override
    public DialogResult showResetDialog() {
        ResetDialog resetDialog = dialogFactory.createResetDialog();
        boolean pressedOk = dialogDisplayer.showDialog(resetDialog, "Reset", true);
        if (pressedOk) {
            return new DialogResult<>(true, null, resetDialog.getResetType());
        }
        return new DialogResult(false, null);
    }
}
