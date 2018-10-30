package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.FileStatusCellRenderer;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

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
class CommitDialog extends JPanel {

    private final Runnable enableOk;
    private final Runnable disableOK;
    private Observable<List<IFileChangeType>> filesToCommit;
    private JEditorPane messagePane;

    @Inject
    public CommitDialog(@Assisted("enable") Runnable pEnableOk, @Assisted("disable") Runnable pDisableOK, @Assisted Observable<List<IFileChangeType>> pFilesToCommit){
        enableOk = pEnableOk;
        disableOK = pDisableOK;
        filesToCommit = pFilesToCommit;
        _initGui();
    }

    String getMessageText(){
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
    private class _CommitTableModel extends AbstractTableModel implements IDiscardable {

        private Disposable disposable;
        private List<IFileChangeType> fileList;

        _CommitTableModel(Observable<List<IFileChangeType>> pFilesToCommit){
            disposable = pFilesToCommit.subscribe(fileToCommit -> fileList = fileToCommit);
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

        @Override
        public void discard() {
            disposable.dispose();
        }
    }

    /**
     * Listen to document changes, disable the OK button if there is no text written by the user
     */
    private class _EmptyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if(e.getDocument().getLength() == 0){
                disableOK.run();
            } else {
                enableOk.run();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if(e.getDocument().getLength() == 0){
                disableOK.run();
            } else {
                enableOk.run();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if(e.getDocument().getLength() == 0){
                disableOK.run();
            } else {
                enableOk.run();
            }
        }
    }

}


