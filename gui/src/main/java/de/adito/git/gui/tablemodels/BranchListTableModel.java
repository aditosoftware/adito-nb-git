package de.adito.git.gui.tablemodels;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BranchListTableModel is the AbstractTableModel for the BranchList.
 *
 * @author A.Arnold 01.10.2018
 */
public class BranchListTableModel extends AbstractTableModel implements IDiscardable
{

  private final Disposable branchesDisposable;
  private final EBranchType branchType;
  private List<IBranch> branches = Collections.emptyList();

  /**
   * @param pBranches   The List of Branches to show
   * @param pBranchType The BranchType for the Branch
   */
  public BranchListTableModel(Observable<Optional<List<IBranch>>> pBranches, @NotNull EBranchType pBranchType)
  {
    branchType = pBranchType;
    branchesDisposable = pBranches.subscribe(pBranchList -> {
      branches = pBranchList.orElse(Collections.emptyList()).stream()
          .filter(pBranch -> pBranch.getType() == branchType)
          .collect(Collectors.toList());
      fireTableDataChanged();
    });
  }

  @Override
  public int getRowCount()
  {
    return branches.size();
  }

  @Override
  public int getColumnCount()
  {
    return 2;
  }

  @Override
  public Object getValueAt(int pRowIndex, int pColumnIndex)
  {
    if (pColumnIndex == 0)
    {
      return branches.get(pRowIndex).getName();
    }
    if (pColumnIndex == 1)
    {
      return branches.get(pRowIndex).getId();
    }
    return null;
  }

  @Override
  public String getColumnName(int pColumn)
  {
    if (pColumn == 0)
    {
      return branchType.getDisplayName();
    }
    if (pColumn == 1)
    {
      return "branchID";
    }
    return null;
  }

  @Override
  public void discard()
  {
    branchesDisposable.dispose();
  }
}
