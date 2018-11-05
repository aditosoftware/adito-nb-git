package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.inject.Singleton;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author m.kaspera 05.11.2018
 */
@Singleton
class ResetAction extends AbstractTableAction implements IDiscardable {


    private final IDialogProvider dialogProvider;
    private final Observable<IRepository> repository;
    private final Observable<List<ICommit>> selectedCommitObservable;
    private final Disposable disposable;

    @Inject
    ResetAction(IDialogProvider pDialogProvider, @Assisted Observable<IRepository> pRepository, @Assisted Observable<List<ICommit>> pSelectedCommitObservable) {
        super("Reset to here");
        dialogProvider = pDialogProvider;
        repository = pRepository;
        selectedCommitObservable = pSelectedCommitObservable;
        disposable = selectedCommitObservable.subscribe(pSelectedCommits -> setEnabled(pSelectedCommits.size() == 1));
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
    protected boolean isEnabled0() {
        return selectedCommitObservable.blockingFirst().size() == 1;
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}
