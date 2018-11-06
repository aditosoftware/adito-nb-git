package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 11.10.2018
 */
class IgnoreAction extends AbstractTableAction {

    private Observable<IRepository> repository;
    private Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    IgnoreAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Ignore", getIsEnabledObservable(pSelectedFilesObservable));
        selectedFilesObservable = pSelectedFilesObservable;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<File> files = selectedFilesObservable.blockingFirst()
                .stream()
                .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
                .collect(Collectors.toList());
        try {
            repository.blockingFirst().ignore(files);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Only enabled if all selected files are not in the index yet, i.e. have status
     * NEW, MODIFY or MISSING
     */
    private static Observable<Boolean> getIsEnabledObservable(Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> selectedFiles
                .stream()
                .allMatch(row ->
                        row.getChangeType().equals(EChangeType.NEW)
                                || row.getChangeType().equals(EChangeType.MODIFY)
                                || row.getChangeType().equals(EChangeType.MISSING)));
    }
}
