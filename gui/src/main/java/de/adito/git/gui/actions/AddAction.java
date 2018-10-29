package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for adding files to staging
 *
 * @author m.kaspera 11.10.2018
 */
class AddAction extends AbstractTableAction {

    private IRepository repository;
    private Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    AddAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Add");
        selectedFilesObservable = pSelectedFilesObservable;
        repository = pRepository.blockingFirst();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileChangeType> fileChangeTypes = selectedFilesObservable.blockingFirst();
        try {
            List<File> files = new ArrayList<>();
            for(IFileChangeType fileChangeType: fileChangeTypes){
                files.add(fileChangeType.getFile());
            }
            repository.add(files);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Not enabled if file is already in index (i.e. has status
     * CHANGED, ADD or DELETE
     */
    @Override
    protected boolean isEnabled0() {
        List<IFileChangeType> fileChangeTypes = selectedFilesObservable.blockingFirst();
        if (fileChangeTypes == null)
            return false;
        return fileChangeTypes.stream()
                .anyMatch(row ->
                        row.getChangeType().equals(EChangeType.CHANGED)
                                || row.getChangeType().equals(EChangeType.ADD)
                                || row.getChangeType().equals(EChangeType.DELETE));
    }
}
