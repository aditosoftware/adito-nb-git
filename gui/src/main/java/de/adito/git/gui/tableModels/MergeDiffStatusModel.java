package de.adito.git.gui.tableModels;

import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author m.kaspera 25.10.2018
 */
public class MergeDiffStatusModel extends AbstractTableModel implements IDiscardable
{

  private List<IMergeDiff> mergeDiffs;
  private final Disposable disposable;

  public MergeDiffStatusModel(Observable<List<IMergeDiff>> pMergeDiffObservable)
  {
    disposable = pMergeDiffObservable.subscribe(pMergeDiffs -> {
      mergeDiffs = pMergeDiffs;
      fireTableDataChanged();
    });
  }

  @Override
  public String getColumnName(int pColumn)
  {
    String columnName;
    if (pColumn == 0)
      columnName = "fileName";
    else if (pColumn == 1)
      columnName = "filePath";
    else
      columnName = super.getColumnName(pColumn);
    return columnName;
  }

  @Override
  public int getRowCount()
  {
    return mergeDiffs.size();
  }

  @Override
  public int getColumnCount()
  {
    return 2;
  }

  @Override
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    String path = mergeDiffs.get(pRowIndex).getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath();
    if (pColumnIndex == 0)
    {
      String[] pathFolders = path.split("/");
      return pathFolders[pathFolders.length - 1];
    }
    else
    {
      return path;
    }
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
