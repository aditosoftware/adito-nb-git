package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.DiffDialog;
import de.adito.git.gui.IDialogDisplayer;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author m.kaspera 12.10.2018
 */
public class DiffAction extends AbstractTableAction {

    private IRepository repository;
    private IDialogDisplayer dialogDisplayer;
    private Supplier<List<IFileChangeType>> selectedFiles;

    public DiffAction(IDialogDisplayer pDialogDisplayer, IRepository pRepository, Supplier<List<IFileChangeType>> pSelectedFiles){
        super("Show Diff");
        repository = pRepository;
        dialogDisplayer = pDialogDisplayer;
        this.selectedFiles = pSelectedFiles;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileDiff> fileDiffs;
        List<IFileChangeType> fileChanges = selectedFiles.get();
        try {
            List<File> files = new ArrayList<>();
            for (IFileChangeType fileChangeType : fileChanges) {
                files.add(fileChangeType.getFile());
            }
            fileDiffs = repository.diff(files);
            DiffDialog diffDialog = new DiffDialog(fileDiffs);
            dialogDisplayer.showDialog(diffDialog, "Diff for files", true);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected boolean isEnabled0() {
        return true;
    }
}
