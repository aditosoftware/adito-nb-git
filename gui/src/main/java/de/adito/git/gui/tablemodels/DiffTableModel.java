package de.adito.git.gui.tablemodels;

import de.adito.git.api.data.IFileDiff;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;

/**
 * TableModel for displaying a table of IFileDiffs
 *
 * @author m.kaspera 07.11.2018
 */
public class DiffTableModel extends AbstractTableModel
{

  protected static final String[] columnNames = {StatusTableModel.FILE_NAME_COLUMN_NAME,
                                                 StatusTableModel.FILE_PATH_COLUMN_NAME,
                                                 StatusTableModel.CHANGE_TYPE_COLUMN_NAME};
  private final List<IFileDiff> fileDiffs;

  public DiffTableModel(List<IFileDiff> pFileDiffs)
  {

    fileDiffs = pFileDiffs;
  }

  @Override
  public Class<?> getColumnClass(int pColumnIndex)
  {
    return String.class;
  }

  @Override
  public int findColumn(String pColumnName)
  {
    for (int index = 0; index < columnNames.length; index++)
    {
      if (getColumnName(index).equals(pColumnName))
        return index;
    }
    return -1;
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
    if (pColumnIndex == findColumn(StatusTableModel.FILE_NAME_COLUMN_NAME))
      returnValue = new File(fileDiffs.get(pRowIndex).getFilePath()).getName();
    if (pColumnIndex == findColumn(StatusTableModel.FILE_PATH_COLUMN_NAME))
      returnValue = fileDiffs.get(pRowIndex).getFilePath();
    else if (pColumnIndex == findColumn(StatusTableModel.CHANGE_TYPE_COLUMN_NAME))
      returnValue = fileDiffs.get(pRowIndex).getChangeType();
    return returnValue;
  }
}
