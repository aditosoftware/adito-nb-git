package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 12.10.2018
 */
class DiffAction extends AbstractTableAction {

    private Observable<IRepository> repository;
    private IDialogProvider dialogProvider;
    private Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    DiffAction(@Assisted Observable<IRepository> pRepository, IDialogProvider pDialogProvider,
                      @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable){
        super("Show Diff", getIsEnabledObservable(pSelectedFilesObservable));
        repository = pRepository;
        dialogProvider = pDialogProvider;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileDiff> fileDiffs;
        try {
            List<File> files = selectedFilesObservable.blockingFirst()
                    .stream()
                    .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
                    .collect(Collectors.toList());
            fileDiffs = repository.blockingFirst().diff(files, null);
            dialogProvider.showDiffDialog(fileDiffs);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static Observable<Boolean> getIsEnabledObservable(Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> true);
    }
}
