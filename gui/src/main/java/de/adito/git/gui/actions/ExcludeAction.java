package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author m.kaspera 11.10.2018
 */
public class ExcludeAction extends AbstractTableAction {

    private IRepository repository;
    private Supplier<List<IFileChangeType>> selectedFiles;

    public ExcludeAction(IRepository pRepository, Supplier<List<IFileChangeType>> pSelectedFiles) {
        super("Exclude");
        repository = pRepository;
        selectedFiles = pSelectedFiles;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileChangeType> fileChanges = selectedFiles.get();
        try {
            List<File> files = new ArrayList<>();
            for (IFileChangeType fileChangeType : fileChanges) {
                files.add(fileChangeType.getFile());
            }
            repository.exclude(files);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected boolean isEnabled0() {
        List<IFileChangeType> fileChangeTypes = selectedFiles.get();
        if (fileChangeTypes == null)
            return false;
        return fileChangeTypes.stream().allMatch(row ->
                row.getChangeType().equals(EChangeType.NEW)
                        || row.getChangeType().equals(EChangeType.MODIFY)
                        || row.getChangeType().equals(EChangeType.MISSING));
    }
}