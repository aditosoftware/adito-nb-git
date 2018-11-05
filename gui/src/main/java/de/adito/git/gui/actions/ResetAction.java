package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.inject.Singleton;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author m.kaspera 05.11.2018
 */
@Singleton
class ResetAction extends AbstractTableAction {


    private final IDialogProvider dialogProvider;
    private final Observable<IRepository> repository;
    private final Observable<List<ICommit>> selectedCommitObservable;

    @Inject
    ResetAction(IDialogProvider pDialogProvider, @Assisted Observable<IRepository> pRepository, @Assisted Observable<List<ICommit>> pSelectedCommitObservable) {
        super("Reset to here");
        dialogProvider = pDialogProvider;
        repository = pRepository;
        selectedCommitObservable = pSelectedCommitObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogResult dialogResult = dialogProvider.showResetDialog();
        if (dialogResult.getInformation() instanceof EResetType) {
            List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst();
            if (selectedCommits.size() == 1) {
                try {
                    repository.blockingFirst().reset(selectedCommits.get(0).getId(), (EResetType) dialogResult.getInformation());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    protected Observable<Boolean> getIsEnabledObservable() {
        return selectedCommitObservable.map(selectedCommits -> selectedCommits.size() == 1);
    }

}
