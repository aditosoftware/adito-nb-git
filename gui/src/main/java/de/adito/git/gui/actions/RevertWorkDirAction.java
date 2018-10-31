package de.adito.git.gui.actions;

import com.google.inject.Inject;
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
class RevertWorkDirAction extends AbstractTableAction {

    private final IRepository repository;
    private final Observable<List<IFileChangeType>> selectedFilesObservable;

    @Inject
    RevertWorkDirAction(@Assisted Observable<IRepository> pRepository,
                        @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Revert");
        repository = pRepository.blockingFirst();
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.revertWorkDir(selectedFilesObservable.blockingFirst().stream().map(IFileChangeType::getFile).collect(Collectors.toList()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected boolean isEnabled0() {
        return selectedFilesObservable.blockingFirst()
                .stream()
                .noneMatch(fileChangeType -> fileChangeType.getChangeType().equals(EChangeType.SAME));
    }
}
