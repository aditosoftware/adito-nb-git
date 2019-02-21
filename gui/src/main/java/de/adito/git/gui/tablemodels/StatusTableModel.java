package de.adito.git.gui.tablemodels;

import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.Optional;

/**
 * Model that describes the contents of the table in the StatusWindow
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusTableModel extends AbstractTableModel implements IDiscardable
{

  public static final String FILE_NAME_COLUMN_NAME = "fileName";
  public static final String FILE_PATH_COLUMN_NAME = "filePath";
  public static final String CHANGE_TYPE_COLUMN_NAME = "changeType";
  public static final String[] columnNames = {FILE_NAME_COLUMN_NAME, FILE_PATH_COLUMN_NAME, CHANGE_TYPE_COLUMN_NAME};

  private IFileStatus status;
  private Disposable statusDisposable;

  public StatusTableModel(Observable<Optional<IFileStatus>> pStatusObservable)
  {
    statusDisposable = pStatusObservable.subscribe(pStatus -> {
      status = pStatus.orElse(null);
      fireTableDataChanged();
    });
  }

  @Override
  public int findColumn(String pColumnName)
  {
    for (int index = 0; index < columnNames.length; index++)
    {
      if (columnNames[index].equals(pColumnName))
        return index;
    }
    return -1;
  }

  @Override
  public int getRowCount()
  {
    if (status == null)
      return 0;
    return status.getUncommitted().size();
  }

  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int pColumn)
  {
    return StatusTableModel.columnNames[pColumn];
  }

  @Override
  public Class<?> getColumnClass(int pColumnIndex)
  {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int pRowIndex, int pColumnIndex)
  {
    return false;
  }

  @Nullable
  @Override
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    // Columns : | filename | filepath     | changetype |
    // example:  | file     | example/file | modified   |
    Object returnValue = null;
    if (status != null)
    {
      if (pColumnIndex == findColumn(FILE_NAME_COLUMN_NAME))
        returnValue = status.getUncommitted().get(pRowIndex).getFile().getName();
      else if (pColumnIndex == findColumn(FILE_PATH_COLUMN_NAME))
        returnValue = status.getUncommitted().get(pRowIndex).getFile().getPath();
      else if (pColumnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
        returnValue = status.getUncommitted().get(pRowIndex).getChangeType();
    }
    return returnValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void discard()
  {
    statusDisposable.dispose();
  }
}
