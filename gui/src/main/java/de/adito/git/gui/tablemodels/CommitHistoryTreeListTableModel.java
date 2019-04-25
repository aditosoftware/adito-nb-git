package de.adito.git.gui.tablemodels;

import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.gui.DateTimeRenderer;
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

public class CommitHistoryTreeListTableModel extends AbstractTableModel
{

  public static final String BRANCHING_COL_NAME = "Branching / Commit Message";
  public static final String AUTHOR_COL_NAME = "Author";
  public static final String DATE_COL_NAME = "Date";

  private static final int BRANCHING = 0;
  private static final int AUTHOR = 1;
  private static final int TIME = 2;

  private static final List<String> columnNames = new ArrayList<>(Arrays.asList(BRANCHING_COL_NAME, AUTHOR_COL_NAME, DATE_COL_NAME));

  private final List<CommitHistoryTreeListItem> commitList;

  /**
   * @param pCommitList the list of commits to show
   */
  public CommitHistoryTreeListTableModel(List<CommitHistoryTreeListItem> pCommitList)
  {
    commitList = pCommitList;
  }

  @Override
  public int findColumn(@NotNull String pColumnName)
  {
    for (int index = 0; index < columnNames.size(); index++)
    {
      if (pColumnName.equals(columnNames.get(index)))
        return index;
    }
    return -1;
  }

  /**
   * Appends the passed data to the current list of the model
   *
   * @param pToAdd List with data to add to the current list
   */
  public void addData(List<CommitHistoryTreeListItem> pToAdd)
  {
    commitList.addAll(pToAdd);
    fireTableDataChanged();
  }

  /**
   * Clears the current list in the model and inserts the passed values afterwards
   *
   * @param pNewValues Values with which to replace the current list
   */
  public void resetData(List<CommitHistoryTreeListItem> pNewValues)
  {
    if (!commitListsEqual(commitList, pNewValues))
    {
      commitList.clear();
      addData(pNewValues);
    }
  }

  @Override
  public String getColumnName(int pColumn)
  {
    return columnNames.get(pColumn);
  }

  @Override
  public int getRowCount()
  {
    return commitList.size();
  }

  @Override
  public int getColumnCount()
  {
    return columnNames.size();
  }

  @Override
  public Class<?> getColumnClass(int pColumnIndex)
  {
    switch (pColumnIndex)
    {
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
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    CommitHistoryTreeListItem commitHistoryTreeListItem = commitList.get(pRowIndex);
    switch (pColumnIndex)
    {
      case BRANCHING:
        return commitHistoryTreeListItem;
      case AUTHOR:
        return commitHistoryTreeListItem.getCommit().getAuthor();
      case TIME:
        return DateTimeRenderer.asString(commitHistoryTreeListItem.getCommit().getTime());
      default:
        return null;
    }
  }

  /**
   * checks if the commits contained in the two CommitHistoryTreeListItem lists are equal and in the same order
   *
   * @param pOldValues List with CommitHistoryTreeListItems
   * @param pNewValues List with CommitHistoryTreeListItems
   * @return true if the same commits are in the same order, false otherwise
   */
  private boolean commitListsEqual(List<CommitHistoryTreeListItem> pOldValues, List<CommitHistoryTreeListItem> pNewValues)
  {
    if (pOldValues.size() != pNewValues.size())
      return false;
    for (int index = 0; index < pNewValues.size(); index++)
    {
      if (!pOldValues.get(index).commitDetailsEquals(pNewValues.get(index)))
        return false;
    }
    return true;
  }

}
