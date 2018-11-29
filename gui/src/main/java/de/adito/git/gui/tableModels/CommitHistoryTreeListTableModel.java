package de.adito.git.gui.tableModels;

import de.adito.git.api.CommitHistoryTreeListItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The table model for the commits
 *
 * @author A.Arnold 02.10.2018
 */

public class CommitHistoryTreeListTableModel extends AbstractTableModel {

    public static final String BRANCHING_COL_NAME = "Branching/Commit Message";
    public static final String AUTHOR_COL_NAME = "Author";
    public static final String DATE_COL_NAME = "Date";

    private List<CommitHistoryTreeListItem> commitList;
    private static final int BRANCHING = 0;
    private static final int AUTHOR = 1;
    private static final int TIME = 2;

    private static final List<String> columnNames = new ArrayList<>(Arrays.asList(BRANCHING_COL_NAME, AUTHOR_COL_NAME, DATE_COL_NAME));

    public static int getColumnIndex(@NotNull String columnName) {
        for (int index = 0; index < columnNames.size(); index++) {
            if (columnName.equals(columnNames.get(index)))
                return index;
        }
        return -1;
    }

    /**
     * @param pCommitList the list of commits to show
     */
    public CommitHistoryTreeListTableModel(List<CommitHistoryTreeListItem> pCommitList) {
        commitList = pCommitList;
    }

    public void addData(List<CommitHistoryTreeListItem> toAdd) {
        commitList.addAll(toAdd);
        fireTableDataChanged();
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
            case AUTHOR:
                return commitHistoryTreeListItem.getCommit().getAuthor();
            case TIME:
                return commitHistoryTreeListItem.getCommit().getTime();
            default:
                return null;
        }
    }

}
