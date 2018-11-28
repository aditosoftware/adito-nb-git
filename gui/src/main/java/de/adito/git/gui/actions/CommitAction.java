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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Action class for showing the commit dialogs and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
class CommitAction extends AbstractTableAction {

    private Observable<Optional<IRepository>> repository;
    private IDialogProvider dialogProvider;
    private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

    @Inject
    CommitAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                 @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        super("Commit", getIsEnabledObservable(pSelectedFilesObservable));
        repository = pRepository;
        dialogProvider = pDialogProvider;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogResult<Supplier<List<IFileChangeType>>> dialogResult = dialogProvider.showCommitDialog(repository, selectedFilesObservable);
        // if user didn't cancel the dialogs
        if (dialogResult.isPressedOk()) {
            try {
                List<File> files = dialogResult.getInformation().get()
                        .stream()
                        .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
                        .collect(Collectors.toList());
                repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).commit(dialogResult.getMessage(), files);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private static Observable<Optional<Boolean>> getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> Optional.of(true));
    }
}
