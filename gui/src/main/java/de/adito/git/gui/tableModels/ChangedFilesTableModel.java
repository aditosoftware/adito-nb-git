package de.adito.git.gui.tableModels;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * TableModel for displaying a the list of changed files with their name and absolute path
 *
 * @author M.Kaspera 03.12.2018
 */
public class ChangedFilesTableModel extends AbstractTableModel implements IDiscardable
{

  public static final String FILE_PATH_COLUMN_NAME = StatusTableModel.FILE_PATH_COLUMN_NAME;
  public static final String FILE_NAME_COLUMN_NAME = StatusTableModel.FILE_NAME_COLUMN_NAME;
  @SuppressWarnings("WeakerAccess")
  public static final String CHANGE_TYPE_COLUMN_NAME = StatusTableModel.CHANGE_TYPE_COLUMN_NAME;
  private static final String[] columnNames = {FILE_NAME_COLUMN_NAME, FILE_PATH_COLUMN_NAME, CHANGE_TYPE_COLUMN_NAME};
  private Disposable disposable;
  private List<IFileChangeType> changedFiles = new ArrayList<>();

  public ChangedFilesTableModel(io.reactivex.Observable<Optional<List<ICommit>>> pSelectedCommits, Observable<Optional<IRepository>> pRepo)
  {
    Optional<IRepository> currentRepo = pRepo.blockingFirst();
    disposable = pSelectedCommits.subscribe(pSelectedCommitsOpt -> {
      Set<IFileChangeType> changedFilesSet = new HashSet<>();
      if (pSelectedCommitsOpt.isPresent() && !pSelectedCommitsOpt.get().isEmpty() && currentRepo.isPresent())
      {
        for (ICommit selectedCommit : pSelectedCommitsOpt.get())
        {
          changedFilesSet.addAll(currentRepo.get().getCommittedFiles(selectedCommit.getId()));
        }
        changedFiles = new ArrayList<>(changedFilesSet);
      }
      else
      {
        changedFiles = Collections.emptyList();
      }
      fireTableDataChanged();
    });
  }

  @Override
  public int findColumn(String pColumnName)
  {
    for (int index = 0; index < pColumnName.length(); index++)
    {
      if (columnNames[index].equals(pColumnName))
      {
        return index;
      }
    }
    return -1;
  }

  @Override
  public String getColumnName(int pColumn)
  {
    String returnValue = "";
    if (pColumn == findColumn(FILE_NAME_COLUMN_NAME))
      returnValue = FILE_NAME_COLUMN_NAME;
    if (pColumn == findColumn(FILE_PATH_COLUMN_NAME))
      returnValue = FILE_PATH_COLUMN_NAME;
    if (pColumn == findColumn(CHANGE_TYPE_COLUMN_NAME))
      returnValue = CHANGE_TYPE_COLUMN_NAME;
    return returnValue;
  }

  @Override
  public int getRowCount()
  {
    return changedFiles.size();
  }

  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    Object returnValue;
    if (pColumnIndex == findColumn(FILE_NAME_COLUMN_NAME))
      returnValue = changedFiles.get(pRowIndex).getFile().getName();
    else if (pColumnIndex == findColumn(FILE_PATH_COLUMN_NAME))
      returnValue = changedFiles.get(pRowIndex).getFile().getAbsolutePath();
    else if (pColumnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
      returnValue = changedFiles.get(pRowIndex).getChangeType();
    else returnValue = changedFiles.get(pRowIndex);
    return returnValue;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
