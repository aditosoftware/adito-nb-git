package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 24.10.2018
 */
class MergeAction extends AbstractTableAction {

    private final Observable<IRepository> repositoryObservable;
    private final IDialogProvider dialogProvider;
    private Observable<Optional<IBranch>> targetBranch;

    @Inject
    MergeAction(IDialogProvider dialogProvider, @Assisted Observable<IRepository> pRepository, @Assisted Observable<Optional<IBranch>> pTargetBranch) {
        super("Merge into Current", getIsEnabledObservable(pTargetBranch));
        this.dialogProvider = dialogProvider;
        repositoryObservable = pRepository;
        targetBranch = pTargetBranch;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        IRepository repository = repositoryObservable.blockingFirst();
        try {
            if (repository.getStatus().blockingFirst().hasUncommittedChanges()) {
                throw new RuntimeException("Un-committed files detected while trying to merge: Implement stashing or commit/undo changes");
            }
            Optional<IBranch> selectedBranch = targetBranch.blockingFirst();
            if (!selectedBranch.isPresent()) {
                throw new RuntimeException();
            }
            List<IMergeDiff> mergeConflictDiffs = repository.merge(repository.getCurrentBranch().blockingFirst(), selectedBranch.get());
            if (mergeConflictDiffs.size() > 0) {
                dialogProvider.showMergeConflictDialog(repositoryObservable, mergeConflictDiffs);
            }
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private static Observable<Boolean> getIsEnabledObservable(Observable<Optional<IBranch>> pTargetBranch) {
        return pTargetBranch.map(Optional::isPresent);
    }
}
