package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.CommitDialog;
import de.adito.git.gui.IDialogDisplayer;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Action class for showing the commit dialog and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
public class CommitAction extends AbstractTableAction {

    private IDialogDisplayer dialogDisplayer;
    private IRepository repository;
    private final Observable<List<IFileChangeType>> selectedFilesObservable;

    public CommitAction(IDialogDisplayer pDialogDisplayer, IRepository pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Commit");
        dialogDisplayer = pDialogDisplayer;
        repository = pRepository;
        selectedFilesObservable = pSelectedFilesObservable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileChangeType> fileChanges = selectedFilesObservable.blockingFirst();
        CommitDialog commitDialog = new CommitDialog(fileChanges, dialogDisplayer);
        boolean doCommit = dialogDisplayer.showDialog(commitDialog, "Commit", false);
        // if user didn't cancel the dialog
        if (doCommit) {
            try {
                List<File> files = new ArrayList<>();
                for (IFileChangeType fileChangeType : fileChanges) {
                    files.add(fileChangeType.getFile());
                }
                repository.commit(commitDialog.getMessageText(), files);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected boolean isEnabled0() {
        return true;
    }
}
