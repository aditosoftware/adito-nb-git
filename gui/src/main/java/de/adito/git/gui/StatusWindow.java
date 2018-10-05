package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.actions.AbstractTableAction;
import de.adito.git.gui.tableModels.StatusTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel {

    private IFileStatus status;
    private IRepository repository;
    private IDialogDisplayer dialogDisplayer;

    public StatusWindow(IFileStatus pStatus, IDialogDisplayer pDialogDisplayer, IRepository repository) {
        dialogDisplayer = pDialogDisplayer;
        status = pStatus;
        this.repository = repository;
        _initGui();
    }

    private void _initGui() {
        setLayout(new BorderLayout());
        JTable statusTable = new JTable(new StatusTableModel(status));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        _initPopupMenu(statusTable);
        add(statusTable, BorderLayout.CENTER);
    }

    /**
     *
     * @param pStatusTable JTable for which to set up the popup menu
     */
    private void _initPopupMenu(JTable pStatusTable) {
        List<AbstractTableAction> actionList = new ArrayList<>();
        actionList.add(new _ShowCommitDialogAction());
        actionList.add(new _AddAction());
        TablePopupMenu tablePopupMenu = new TablePopupMenu(pStatusTable, actionList);
        tablePopupMenu.activateMouseListener();

    }

    /**
     * Action class for showing the commit dialog and implementing the commit functionality
     */
    private class _ShowCommitDialogAction extends AbstractTableAction {

        _ShowCommitDialogAction() {
            super("commit");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<IFileChangeType> selectedFiles = new ArrayList<>();
            for (int rowNum : rows) {
                selectedFiles.add(status.getUncommitted().get(rowNum));
            }
            CommitDialog commitDialog = new CommitDialog(selectedFiles);
            boolean doCommit = dialogDisplayer.showDialog(commitDialog, "Commit");
            // if user didn't cancel the dialog
            if (doCommit) {
                // if all files are selected just commit everything
                if (rows.length == status.getUncommitted().size()) {
                    try {
                        repository.commit(commitDialog.getMessageText());
                        status = repository.status();
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
                        status = repository.status();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Action for adding files to staging
     */
    private class _AddAction extends AbstractTableAction {

        _AddAction() {
            super("Add");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<File> selectedFiles = new ArrayList<>();
            for (int rowNum : rows) {
                selectedFiles.add(status.getUncommitted().get(rowNum).getFile());
            }
            try {
                repository.add(selectedFiles);
                status = repository.status();
                revalidate();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
