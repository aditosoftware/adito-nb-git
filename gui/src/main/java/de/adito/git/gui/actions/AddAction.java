package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Action for adding files to staging
 *
 * @author m.kaspera 11.10.2018
 */
class AddAction extends AbstractTableAction {

    private Observable<Optional<IRepository>> repository;
    private Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

    @Inject
    AddAction(@Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        super("Add", getIsEnabledObservable(pSelectedFilesObservable));
        selectedFilesObservable = pSelectedFilesObservable;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            List<File> files = selectedFilesObservable.blockingFirst()
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
                    .collect(Collectors.toList());
            repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).add(files);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * Not enabled if file is already in index (i.e. has status
     * MODIFY, ADD or DELETE
     */
    private static Observable<Optional<Boolean>> getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> Optional.of(selectedFiles
                .orElse(Collections.emptyList())
                .stream().anyMatch(row ->
                EChangeType.CHANGED.equals(row.getChangeType())
                        || EChangeType.ADD.equals(row.getChangeType())
                        || EChangeType.DELETE.equals(row.getChangeType()))));
    }

}
