package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.CommitDialog;
import de.adito.git.gui.IDialogDisplayer;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Action class for showing the commit dialog and implementing the commit functionality
 *
 * @author m.kaspera 11.10.2018
 */
public class CommitAction extends AbstractTableAction {

    private IDialogDisplayer dialogDisplayer;
    private IRepository repository;
    private IFileStatus status;

    public CommitAction(IDialogDisplayer pDialogDisplayer, IRepository pRepository, IFileStatus pStatus) {
        super("Commit");
        dialogDisplayer = pDialogDisplayer;
        repository = pRepository;
        status = pStatus;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<IFileChangeType> selectedFiles = new ArrayList<>();
        for (int rowNum : rows) {
            selectedFiles.add(status.getUncommitted().get(rowNum));
        }
        CommitDialog commitDialog = new CommitDialog(selectedFiles, dialogDisplayer);
        boolean doCommit = dialogDisplayer.showDialog(commitDialog, "Commit", false);
        // if user didn't cancel the dialog
        if (doCommit) {
            // if all files are selected just commit everything
            if (rows.length == status.getUncommitted().size()) {
                try {
                    repository.commit(commitDialog.getMessageText());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                //if only a few files are selected only commit those select few files
            } else {
                List<File> filesToCommit = new ArrayList<>();
                for (IFileChangeType changeType : selectedFiles) {
                    filesToCommit.add(changeType.getFile());
                }
                try {
                    repository.commit(commitDialog.getMessageText(), filesToCommit);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    protected boolean filter(int[] rows) {
        return true;
    }
}
