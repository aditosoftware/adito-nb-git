package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 31.10.2018
 */
class RevertWorkDirAction extends AbstractTableAction {

    private final Observable<Optional<IRepository>> repository;
    private final Observable<Optional<List<IFileChangeType>>> selectedFilesObservable;

    @Inject
    RevertWorkDirAction(@Assisted Observable<Optional<IRepository>> pRepository,
                        @Assisted Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        super("Revert", getIsEnabledObservable(pSelectedFilesObservable));
        repository = pRepository;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.blockingFirst()
                    .orElseThrow(() -> new RuntimeException("no valid repository found"))
                    .revertWorkDir(selectedFilesObservable.blockingFirst()
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(IFileChangeType::getFile)
                            .collect(Collectors.toList()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static Observable<Optional<Boolean>> getIsEnabledObservable(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> Optional.of(selectedFiles
                .orElse(Collections.emptyList())
                .stream()
                .noneMatch(fileChangeType -> fileChangeType.getChangeType().equals(EChangeType.SAME))));
    }
}
