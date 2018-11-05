package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author m.kaspera 24.10.2018
 */
class MergeAction extends AbstractTableAction {

    private final Observable<IRepository> repositoryObservable;
    private final IDialogProvider dialogProvider;
    private String currentBranch;
    private String targetBranch;

    @Inject
    MergeAction(IDialogProvider dialogProvider, @Assisted Observable<IRepository> pRepository, @Assisted("current") String pCurrentBranch, @Assisted("target") String pTargetBranch){
        super("merge with");
        this.dialogProvider = dialogProvider;
        repositoryObservable = pRepository;
        currentBranch = pCurrentBranch;
        targetBranch = pTargetBranch;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            List<IMergeDiff> mergeConflictDiffs = repositoryObservable.blockingFirst().merge(currentBranch, targetBranch);
            if(mergeConflictDiffs.size() > 0){
                dialogProvider.showMergeConflictDialog(repositoryObservable, mergeConflictDiffs);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected Observable<Boolean> getIsEnabledObservable() {
        return BehaviorSubject.createDefault(false);
    }
}
