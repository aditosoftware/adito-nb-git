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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 11.10.2018
 */
class ExcludeAction extends AbstractTableAction {

    private Observable<Optional<IRepository>> repository;
    private Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

    @Inject
    ExcludeAction(@Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        super("Exclude", getIsEnabledObservable(pSelectedFilesObservable));
        repository = pRepository;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            List<File> files = selectedFilesObservable.blockingFirst()
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(iFileChangeType -> new File(iFileChangeType.getFile().getPath()))
                    .collect(Collectors.toList());
            repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).exclude(files);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static Observable<Optional<Boolean>> getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> Optional.of(selectedFiles
                .orElse(Collections.emptyList())
                .stream()
                .allMatch(row ->
                row.getChangeType().equals(EChangeType.NEW)
                        || row.getChangeType().equals(EChangeType.MODIFY)
                        || row.getChangeType().equals(EChangeType.MISSING))));
    }
}
