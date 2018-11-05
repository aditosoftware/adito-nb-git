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
class AddAction extends AbstractTableAction implements IDiscardable {

    private final Disposable disposable;
    private IRepository repository;
    private Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    AddAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Add");
        selectedFilesObservable = pSelectedFilesObservable;
        repository = pRepository.blockingFirst();
        disposable = selectedFilesObservable.subscribe(selectedFiles -> this.setEnabled(isEnabled0()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            List<File> files = selectedFilesObservable.blockingFirst().stream().map(IFileChangeType::getFile).collect(Collectors.toList());
            repository.add(files);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Not enabled if file is already in index (i.e. has status
     * MODIFY, ADD or DELETE
     */
    @Override
    protected boolean isEnabled0() {
        List<IFileChangeType> fileChangeTypes = selectedFilesObservable.blockingFirst();
        if (fileChangeTypes == null)
            return false;
        return fileChangeTypes.stream()
                .anyMatch(row ->
                        EChangeType.CHANGED.equals(row.getChangeType())
                                || EChangeType.ADD.equals(row.getChangeType())
                                || EChangeType.DELETE.equals(row.getChangeType()));
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}
