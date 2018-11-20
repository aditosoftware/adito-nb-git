package de.adito.git.gui.tableModels;

import de.adito.git.api.CommitHistoryTreeListItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The table model for the commits
 *
 * @author A.Arnold 02.10.2018
 */

public class CommitListTableModel extends AbstractTableModel {

    private List<CommitHistoryTreeListItem> commitList;
    private static final int BRANCHING = 0;
    private static final int MESSAGE = 1;
    private static final int AUTHOR = 2;
    private static final int TIME = 3;

    private static final List<String> columnNames = new ArrayList<>(Arrays.asList("Branches", "Message", "Author", "Time"));

    /**
     * @param pCommitList the list of commits to show
     */
    public CommitListTableModel(List<CommitHistoryTreeListItem> pCommitList) {
        commitList = pCommitList;
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
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case BRANCHING:
                return CommitHistoryTreeListItem.class;
            case MESSAGE:
                return String.class;
            case AUTHOR:
                return String.class;
            case TIME:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CommitHistoryTreeListItem commitHistoryTreeListItem = commitList.get(rowIndex);
        switch (columnIndex) {
            case BRANCHING:
                return commitHistoryTreeListItem;
            case MESSAGE:
                return commitHistoryTreeListItem.getCommit().getShortMessage();
            case AUTHOR:
                return commitHistoryTreeListItem.getCommit().getAuthor();
            case TIME:
                return commitHistoryTreeListItem.getCommit().getTime();
            default:
                return null;
        }
    }

}
