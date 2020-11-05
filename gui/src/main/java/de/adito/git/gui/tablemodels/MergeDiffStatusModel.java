package de.adito.git.gui.tablemodels;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.EConflictSide;
import de.adito.git.api.data.diff.IMergeData;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.kaspera 25.10.2018
 */
public class MergeDiffStatusModel extends AbstractTableModel implements IDiscardable
{

  private final List<String> columnNames;
  private List<IMergeData> mergeDiffs = new ArrayList<>();
  private final Disposable disposable;

  public MergeDiffStatusModel(@NotNull Observable<List<IMergeData>> pMergeDiffObservable, @NotNull IMergeDetails pMergeDetails)
  {
    columnNames = List.of("Filename", "Filepath", pMergeDetails.getYoursOrigin(), pMergeDetails.getTheirsOrigin());
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
    if (pColumn < columnNames.size())
    {
      return columnNames.get(pColumn);
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
    return columnNames.size();
  }

  @Override
  public int findColumn(String pColumnName)
  {
    for (int index = 0; index < columnNames.size(); index++)
    {
      if (pColumnName.equals(columnNames.get(index)))
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
