package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Action class for showing the commit dialogs and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
@Singleton
class CommitAction extends AbstractTableAction {

    private IRepository repository;
    private IDialogProvider dialogProvider;
    private final Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    CommitAction(IDialogProvider pDialogProvider, @Assisted Observable<IRepository> pRepository,
                 @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Commit");
        repository = pRepository.blockingFirst();
        dialogProvider = pDialogProvider;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogResult dialogResult = dialogProvider.showCommitDialog(selectedFilesObservable);
        // if user didn't cancel the dialogs
        if (dialogResult.isPressedOk()) {
            try {
                List<File> files = selectedFilesObservable.blockingFirst().stream().map(IFileChangeType::getFile).collect(Collectors.toList());
                repository.commit(dialogResult.getMessage(), files);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected Observable<Boolean> getIsEnabledObservable() {
        return selectedFilesObservable.map(selectedFiles -> true);
    }
}
