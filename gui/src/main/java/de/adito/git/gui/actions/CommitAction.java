package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Action class for showing the commit dialog and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
class CommitAction extends AbstractTableAction {

    private IRepository repository;
    private IDialogProvider dialogProvider;
    private final Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    CommitAction(@Assisted Observable<IRepository> pRepository, IDialogProvider pDialogProvider,
                        @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Commit");
        repository = pRepository.blockingFirst();
        dialogProvider = pDialogProvider;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogResult dialogResult = dialogProvider.showCommitDialog(selectedFilesObservable);
        // if user didn't cancel the dialog
        if (dialogResult.isPressedOk()) {
            try {
                List<File> files = new ArrayList<>();
                for (IFileChangeType fileChangeType : selectedFilesObservable.blockingFirst()) {
                    files.add(fileChangeType.getFile());
                }
                repository.commit(dialogResult.getMessage(), files);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected boolean isEnabled0() {
        return true;
    }
}
