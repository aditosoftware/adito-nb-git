package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 31.10.2018
 */
@Singleton
class ResetFilesAction extends AbstractTableAction {

    private final Observable<IRepository> repository;
    private final Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    ResetFilesAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Reset");
        repository = pRepository;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.blockingFirst().reset(selectedFilesObservable.blockingFirst().stream().map(IFileChangeType::getFile).collect(Collectors.toList()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected Observable<Boolean> getIsEnabledObservable() {
        return selectedFilesObservable.map(selectedFiles -> selectedFiles.stream()
                .noneMatch(fileChangeType -> fileChangeType.getChangeType().equals(EChangeType.SAME)));
    }

}
