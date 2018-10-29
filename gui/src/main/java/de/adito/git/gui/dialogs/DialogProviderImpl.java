package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.gui.guice.IRepositoryFactory;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class DialogProviderImpl implements IDialogProvider
{
    private final IDialogDisplayer dialogDisplayer;
    private final IRepositoryFactory repositoryFactory;
    private final IDialogFactory dialogFactory;

    @Inject
    public DialogProviderImpl(IDialogDisplayer pDialogDisplayer, IRepositoryFactory pRepositoryFactory,
                              IDialogFactory pDialogFactory) {
        dialogDisplayer = pDialogDisplayer;
        repositoryFactory = pRepositoryFactory;
        dialogFactory = pDialogFactory;
    }

    @Override
    public DialogResult createMergeConflictDialog(Observable<IRepository> pRepository, List<IMergeDiff> pMergeConflictDiffs) {
        boolean pressedOk = dialogDisplayer.showDialog(dialogFactory.create(pRepository, pMergeConflictDiffs), "Merge Conflicts", true);
        return new DialogResult(pressedOk, null);
    }

    @Override
    public DialogResult createMergeConflictResolutionDialog(IMergeDiff pMergeDiff) {
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
        CommitDialog commitDialog = dialogFactory.createCommitDialog(pFilesToCommit);
        boolean pressedOk = dialogDisplayer.showDialog(commitDialog, "Commit", false);
        if(pressedOk) {
            commitMessage = commitDialog.getMessageText();
        }
        return new DialogResult(pressedOk, commitMessage);
    }
}
