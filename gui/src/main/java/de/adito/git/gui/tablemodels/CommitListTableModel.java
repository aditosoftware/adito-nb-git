package de.adito.git.gui.tablemodels;

import de.adito.git.api.data.ICommit;
import de.adito.git.gui.DateTimeRenderer;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TableModel for a list of Commits
 *
 * @author M.Kaspera 13.12.2018
 */
public class CommitListTableModel extends AbstractTableModel
{

  private static final String SHORT_MSG_COL_NAME = "Short commit message";
  private static final String AUTHOR_COL_NAME = "Author";
  private static final String DATE_COL_NAME = "Date";
  public static final String COMMIT_OBJ_COL_NAME = "Commit column";

  private static final List<String> columnNames = new ArrayList<>(Arrays.asList(COMMIT_OBJ_COL_NAME, SHORT_MSG_COL_NAME, AUTHOR_COL_NAME,
                                                                                DATE_COL_NAME));
  private final List<ICommit> commitList;

  public CommitListTableModel(List<ICommit> pCommitList)
  {
    commitList = pCommitList;
  }

  @Override
  public String getColumnName(int pColumn)
  {
    if (pColumn >= 0 && pColumn < columnNames.size())
      return columnNames.get(pColumn);
    else return super.getColumnName(pColumn);
  }

  @Override
  public int findColumn(String pColumnName)
  {
    int returnValue = -1;
    if (COMMIT_OBJ_COL_NAME.equals(pColumnName))
      returnValue = 0;
    else if (SHORT_MSG_COL_NAME.equals(pColumnName))
      returnValue = 1;
    else if (AUTHOR_COL_NAME.equals(pColumnName))
      returnValue = 2;
    else if (DATE_COL_NAME.equals(pColumnName))
      returnValue = 3;
    return returnValue;
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
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    Object returnValue = null;
    if (pColumnIndex == findColumn(COMMIT_OBJ_COL_NAME))
      returnValue = commitList.get(pRowIndex);
    else if (pColumnIndex == findColumn(SHORT_MSG_COL_NAME))
      returnValue = commitList.get(pRowIndex).getShortMessage();
    else if (pColumnIndex == findColumn(AUTHOR_COL_NAME))
      returnValue = commitList.get(pRowIndex).getAuthor();
    else if (pColumnIndex == findColumn(DATE_COL_NAME))
      returnValue = DateTimeRenderer.asString(commitList.get(pRowIndex).getTime());
    return returnValue;
  }
}
