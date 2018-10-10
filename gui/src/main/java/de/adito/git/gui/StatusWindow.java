package de.adito.git.gui;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.actions.AbstractTableAction;
import de.adito.git.gui.tableModels.StatusTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel implements IStatusWindow {

    private IFileStatus status;
    private IRepository repository;
    private IDialogDisplayer dialogDisplayer;
    private JTable statusTable;

    @Inject
    public StatusWindow(IDialogDisplayer pDialogDisplayer, RepositoryProvider repository) {
        dialogDisplayer = pDialogDisplayer;
        this.repository = repository.getRepositoryImpl();
        status = this.repository.status();
        _initGui();
    }

    private void _initGui() {
        setLayout(new BorderLayout());
        statusTable = new JTable(new StatusTableModel(status));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // CellRenderer so the files are colored according to their EChangeType
        for (int index = 0; index < statusTable.getColumnModel().getColumnCount(); index++) {
            statusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }

        _initPopupMenu(statusTable);
        add(statusTable, BorderLayout.CENTER);
    }

    /**
     * @param pStatusTable JTable for which to set up the popup menu
     */
    private void _initPopupMenu(JTable pStatusTable) {
        List<AbstractTableAction> actionList = new ArrayList<>();
        actionList.add(new _ShowCommitDialogAction());
        actionList.add(new _AddAction());
        actionList.add(new _IgnoreAction());
        actionList.add(new _ExcludeAction());
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
            CommitDialog commitDialog = new CommitDialog(selectedFiles, dialogDisplayer);
            boolean doCommit = dialogDisplayer.showDialog(commitDialog, "Commit", false);
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
                        // refresh table view
                        status = repository.status();
                        ((StatusTableModel) statusTable.getModel()).statusChanged(status);
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
                // refresh table view
                status = repository.status();
                ((StatusTableModel) statusTable.getModel()).statusChanged(status);
                revalidate();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Not enabled if file is already in index (i.e. has status
         * CHANGED, ADD or DELETE
         *
         * @param rowNumbers the rows selected by the user
         */
        @Override
        public void setRows(int[] rowNumbers) {
            rows = rowNumbers;
            if (Arrays.stream(rowNumbers)
                    .anyMatch(row ->
                            statusTable.getValueAt(row, 2).equals(EChangeType.CHANGED)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.ADD)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.DELETE))) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }
        }

        @Override
        protected boolean filter(int[] rows) {
            return Arrays.stream(rows)
                    .anyMatch(row ->
                            statusTable.getValueAt(row, 2).equals(EChangeType.CHANGED)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.ADD)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.DELETE));
        }
    }

    private class _IgnoreAction extends AbstractTableAction {

        _IgnoreAction() {
            super("Ignore");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<File> selectedFiles = new ArrayList<>();
            for (int rowNum : rows) {
                selectedFiles.add(status.getUncommitted().get(rowNum).getFile());
            }
            try {
                repository.ignore(selectedFiles);
                // refresh table view
                status = repository.status();
                ((StatusTableModel) statusTable.getModel()).statusChanged(status);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Only enabled if all selected files are not in the index yet, i.e. have status
         * NEW, MODIFY or MISSING
         */
        @Override
        protected boolean filter(int[] rows) {
            return Arrays.stream(rows)
                    .allMatch(row ->
                            statusTable.getValueAt(row, 2).equals(EChangeType.NEW)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.MODIFY)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.MISSING));
        }
    }

    private class _ExcludeAction extends AbstractTableAction {

        _ExcludeAction() {
            super("Exclude");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<File> selectedFiles = new ArrayList<>();
            for (int rowNum : rows) {
                selectedFiles.add(status.getUncommitted().get(rowNum).getFile());
            }
            try {
                repository.exclude(selectedFiles);
                // refresh table view
                status = repository.status();
                ((StatusTableModel) statusTable.getModel()).statusChanged(status);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Only enabled if all selected files are not in the index yet, i.e. have status
         * NEW, MODIFY or MISSING
         */
        @Override
        protected boolean filter(int[] rows) {
            return Arrays.stream(rows)
                    .allMatch(row ->
                            statusTable.getValueAt(row, 2).equals(EChangeType.NEW)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.MODIFY)
                                    || statusTable.getValueAt(row, 2).equals(EChangeType.MISSING));
        }
    }

}
