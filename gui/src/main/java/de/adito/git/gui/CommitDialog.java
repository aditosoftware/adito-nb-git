package de.adito.git.gui;

import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.tableModels.StatusTableModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Commit window
 *
 * @author m.kaspera 04.10.2018
 */
public class CommitDialog extends JPanel {

    private List<IFileChangeType> filesToCommit;
    private IDialogDisplayer dialogDisplayer;
    private JEditorPane messagePane;

    public CommitDialog(List<IFileChangeType> pFilesToCommit, IDialogDisplayer pDialogDisplayer){
        filesToCommit = pFilesToCommit;
        dialogDisplayer = pDialogDisplayer;
        _initGui();
    }

    public String getMessageText(){
        return messagePane.getText();
    }

    /**
     * initialise GUI elements
     */
    private void _initGui(){
        setLayout(new BorderLayout());
        JTable fileStatusTable = new JTable(new _CommitTableModel(filesToCommit));
        // Hide the status column from view, but leave the data (retrieved via table.getModel.getValueAt)
        fileStatusTable.getColumnModel().removeColumn(fileStatusTable.getColumn(StatusTableModel.columnNames[2]));
        // Set Renderer for cells so they are colored according to their EChangeType
        for(int index = 0; index < fileStatusTable.getColumnModel().getColumnCount(); index++) {
            fileStatusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }
        // Size for the Table with the list of files to commit
        fileStatusTable.getColumnModel().getColumn(1).setMinWidth(300);
        fileStatusTable.setPreferredSize(new Dimension(550, 750));
        // EditorPane for the Commit message
        messagePane = new JEditorPane();
        messagePane.setPreferredSize(new Dimension(650, 750));
        // Listener for enabling/disabling the OK button
        messagePane.getDocument().addDocumentListener(new _EmptyDocumentListener());
        // Splitpane so the user can choose how big each element should be
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messagePane, fileStatusTable);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * TableModel for the Table, has the list of files to commit. Similar to {@link StatusTableModel}
     */
    private class _CommitTableModel extends AbstractTableModel {


        private List<IFileChangeType> fileList;

        _CommitTableModel(List<IFileChangeType> pFileList){

            fileList = pFileList;
        }

        @Override
        public String getColumnName(int column) {
            return StatusTableModel.columnNames[column];
        }

        @Override
        public int getRowCount() {
            return fileList.size();
        }

        @Override
        public int getColumnCount() {
            return StatusTableModel.columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex == 0) {
                return fileList.get(rowIndex).getFile().getName();
            } else if(columnIndex == 1){
                return fileList.get(rowIndex).getFile().getPath();
            } else if(columnIndex == 2){
                return fileList.get(rowIndex).getChangeType();
            }
            return null;
        }
    }

    /**
     * Listen to document changes, disable the OK button if there is no text written by the user
     */
    private class _EmptyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if(e.getDocument().getLength() == 0){
                dialogDisplayer.disableOKButton();
            } else {
                dialogDisplayer.enableOKButton();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if(e.getDocument().getLength() == 0){
                dialogDisplayer.disableOKButton();
            } else {
                dialogDisplayer.enableOKButton();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if(e.getDocument().getLength() == 0){
                dialogDisplayer.disableOKButton();
            } else {
                dialogDisplayer.enableOKButton();
            }
        }
    }

}


