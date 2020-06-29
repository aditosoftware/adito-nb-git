package de.adito.git.gui.tablemodels;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author m.kaspera 25.10.2018
 */
public class MergeDiffStatusModel extends AbstractTableModel implements IDiscardable
{

  private static final List<String> COLUMN_NAMES = List.of("Filename", "Filepath", "Your Changes", "Their Changes");
  private List<IMergeData> mergeDiffs = new ArrayList<>();
  private final Disposable disposable;

  public MergeDiffStatusModel(Observable<List<IMergeData>> pMergeDiffObservable)
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
    if (pColumn < COLUMN_NAMES.size())
    {
      return COLUMN_NAMES.get(pColumn);
    }
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
    return COLUMN_NAMES.size();
  }

  @Override
  public int findColumn(String pColumnName)
  {
    for (int index = 0; index < COLUMN_NAMES.size(); index++)
    {
      if (pColumnName.equals(COLUMN_NAMES.get(index)))
        return index;
    }
    return -1;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    return IMergeData.class;
  }

  @Override
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    return mergeDiffs.get(pRowIndex);
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
  private boolean _containsSameFiles(List<IMergeData> pList1, List<IMergeData> pList2)
  {
    if (pList1 != null && pList2 != null && pList1.size() == pList2.size())
    {
      for (int index = 0; index < pList1.size(); index++)
      {
        if (!pList1.get(index).getDiff(EConflictSide.YOURS).getFileHeader().getFilePath()
            .equals(pList2.get(index).getDiff(EConflictSide.YOURS).getFileHeader().getFilePath()))
          return false;
      }
      return true;
    }
    return false;
  }
}
