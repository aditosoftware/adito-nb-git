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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Action class for showing the commit dialogs and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
class CommitAction extends AbstractTableAction {

    private Observable<IRepository> repository;
    private IDialogProvider dialogProvider;
    private final Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    CommitAction(IDialogProvider pDialogProvider, @Assisted Observable<IRepository> pRepository,
                 @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Commit", getIsEnabledObservable(pSelectedFilesObservable));
        repository = pRepository;
        dialogProvider = pDialogProvider;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogResult dialogResult = dialogProvider.showCommitDialog(selectedFilesObservable);
        // if user didn't cancel the dialogs
        if (dialogResult.isPressedOk()) {
            File repoTopLevel = repository.blockingFirst().getTopLevelDirectory();
            try {
                List<File> files = selectedFilesObservable.blockingFirst()
                        .stream()
                        .map(iFileChangeType -> new File(repoTopLevel, iFileChangeType.getFile().getPath()))
                        .collect(Collectors.toList());
                repository.blockingFirst().commit(dialogResult.getMessage(), files);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private static Observable<Boolean> getIsEnabledObservable(Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> true);
    }
}
