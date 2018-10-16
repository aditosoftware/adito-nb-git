package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Action for adding files to staging
 *
 * @author m.kaspera 11.10.2018
 */
public class AddAction extends AbstractTableAction {

    private IRepository repository;
    private Supplier<List<IFileChangeType>> selectedFiles;

    public AddAction(IRepository pRepository, Supplier<List<IFileChangeType>> pSelectedFiles) {
        super("Add");
        selectedFiles = pSelectedFiles;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileChangeType> fileChanges = selectedFiles.get();
        try {
            List<File> files = new ArrayList<>();
            for(IFileChangeType fileChangeType: fileChanges){
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
        List<IFileChangeType> files = selectedFiles.get();
        if (files == null)
            return false;
        return files.stream()
                .anyMatch(row ->
                        row.getChangeType().equals(EChangeType.CHANGED)
                                || row.getChangeType().equals(EChangeType.ADD)
                                || row.getChangeType().equals(EChangeType.DELETE));
    }
}
