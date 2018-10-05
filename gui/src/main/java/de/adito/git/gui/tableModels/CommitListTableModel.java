package de.adito.git.gui.tableModels;

import de.adito.git.api.data.ICommit;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author A.Arnold 02.10.2018
 *
 */

public class CommitListTableModel extends AbstractTableModel {

    private List<ICommit> commitList;
    private final int MESSAGE = 0;
    private final int AUTHOR = 1;
    private final int TIME = 2;

    private Map<Integer, String> columnNames = new HashMap<>();

    public CommitListTableModel(List<ICommit> pCommitList) {
        commitList = pCommitList;
        columnNames.put(MESSAGE, "Message");
        columnNames.put(AUTHOR, "Author");
        columnNames.put(TIME, "Time");
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public int getRowCount() {
        return commitList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ICommit commit = commitList.get(rowIndex);
        switch (columnIndex){
            case MESSAGE: return commit.getShortMessage();
            case AUTHOR: return commit.getAuthor();
            case TIME: return commit.getTime();
        }
        return null;
    }

}
