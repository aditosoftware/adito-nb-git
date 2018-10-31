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
class ExcludeAction extends AbstractTableAction {

    private IRepository repository;
    private Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    ExcludeAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Exclude");
        repository = pRepository.blockingFirst();
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            List<File> files = selectedFilesObservable.blockingFirst().stream().map(IFileChangeType::getFile).collect(Collectors.toList());
            repository.exclude(files);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected boolean isEnabled0() {
        List<IFileChangeType> fileChangeTypes = selectedFilesObservable.blockingFirst();
        if (fileChangeTypes == null)
            return false;
        return fileChangeTypes.stream().allMatch(row ->
                row.getChangeType().equals(EChangeType.NEW)
                        || row.getChangeType().equals(EChangeType.MODIFY)
                        || row.getChangeType().equals(EChangeType.MISSING));
    }
}
