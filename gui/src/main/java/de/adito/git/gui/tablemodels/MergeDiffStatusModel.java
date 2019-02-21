package de.adito.git.gui.tablemodels;

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
      boolean sameFiles = _containsSameFiles(mergeDiffs, pMergeDiffs);
      mergeDiffs = pMergeDiffs;
      if (!sameFiles)
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

  /**
   * If either of the lists is null, their size differs, or element i in one list does not equal element in the other list, this function returns
   * false
   *
   * @param pList1 first list of the pair to compare
   * @param pList2 second list of the pair to compare
   * @return if the lists differ from each other false, if they have the same size and same elements true
   */
  private boolean _containsSameFiles(List<IMergeDiff> pList1, List<IMergeDiff> pList2)
  {
    if (pList1 != null && pList2 != null && pList1.size() == pList2.size())
    {
      for (int index = 0; index < pList1.size(); index++)
      {
        if (!pList1.get(index).getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath()
            .equals(pList2.get(index).getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath()))
          return false;
      }
      return true;
    }
    return false;
  }
}
