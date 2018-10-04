package de.adito.git.gui;

import de.adito.git.api.data.IFileChangeType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 *
 *
 * @author m.kaspera 04.10.2018
 */
public class CommitDialog extends JPanel {

    private List<IFileChangeType> filesToCommit;
    private JEditorPane messagePane;

    public CommitDialog(List<IFileChangeType> pFilesToCommit){
        filesToCommit = pFilesToCommit;
        _initGui();
    }

    public String getMessageText(){
        return messagePane.getText();
    }

    private void _initGui(){
        setLayout(new BorderLayout());
        JTable fileStatusTable = new JTable(new _CommitTableModel(filesToCommit));
        fileStatusTable.getColumnModel().getColumn(1).setMinWidth(300);
        fileStatusTable.setPreferredSize(new Dimension(550, 750));
        messagePane = new JEditorPane();
        messagePane.setPreferredSize(new Dimension(650, 750));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messagePane, fileStatusTable);
        add(splitPane, BorderLayout.CENTER);
    }

    private class _CommitTableModel extends AbstractTableModel {


        private List<IFileChangeType> fileList;

        _CommitTableModel(List<IFileChangeType> pFileList){

            fileList = pFileList;
        }

        @Override
        public int getRowCount() {
            return fileList.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex == 0) {
                return fileList.get(rowIndex).getFile().getName();
            } else if(columnIndex == 1){
                return fileList.get(rowIndex).getFile().getPath();
            }
            return null;
        }
    }

}


