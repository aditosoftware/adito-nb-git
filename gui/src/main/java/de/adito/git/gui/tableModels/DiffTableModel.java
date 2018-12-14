package de.adito.git.gui.tableModels;

import de.adito.git.api.data.IFileDiff;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * TableModel for displaying a table of IFileDiffs
 *
 * @author m.kaspera 07.11.2018
 */
public class DiffTableModel extends AbstractTableModel
{

  public static final String[] columnNames = {"Filepath", "Change type"};
  private final List<IFileDiff> fileDiffs;

  public DiffTableModel(List<IFileDiff> pFileDiffs)
  {

    fileDiffs = pFileDiffs;
  }

  @Override
  public String getColumnName(int pColumn)
  {
    if (pColumn >= 0 && pColumn < columnNames.length)
      return columnNames[pColumn];
    return super.getColumnName(pColumn);
  }

  @Override
  public int getRowCount()
  {
    return fileDiffs.size();
  }

  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    Object returnValue = null;
    if (pColumnIndex == 0)
    {
      returnValue = fileDiffs.get(pRowIndex).getFilePath();
    }
    else if (pColumnIndex == 1)
      returnValue = fileDiffs.get(pRowIndex).getChangeType();
    return returnValue;
  }
}
