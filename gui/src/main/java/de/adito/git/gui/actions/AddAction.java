package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Action for adding files to staging
 *
 * @author m.kaspera 11.10.2018
 */
class AddAction extends AbstractTableAction {

    private Observable<IRepository> repository;
    private Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    AddAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Add", getIsEnabledObservable(pSelectedFilesObservable));
        selectedFilesObservable = pSelectedFilesObservable;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File repoTopLevel = repository.blockingFirst().getTopLevelDirectory();
        try {
            List<File> files = selectedFilesObservable.blockingFirst()
                    .stream()
                    .map(iFileChangeType -> new File(repoTopLevel, iFileChangeType.getFile().getPath()))
                    .collect(Collectors.toList());
            repository.blockingFirst().add(files);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Not enabled if file is already in index (i.e. has status
     * MODIFY, ADD or DELETE
     */
    private static Observable<Boolean> getIsEnabledObservable(Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return pSelectedFilesObservable.map(selectedFiles -> selectedFiles.stream().anyMatch(row ->
                EChangeType.CHANGED.equals(row.getChangeType())
                        || EChangeType.ADD.equals(row.getChangeType())
                        || EChangeType.DELETE.equals(row.getChangeType())));
    }

}
