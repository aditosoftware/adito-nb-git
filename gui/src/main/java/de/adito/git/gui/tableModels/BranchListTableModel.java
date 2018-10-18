package de.adito.git.gui.tableModels;

import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BranchListTableModel is the AbstractTableModel for the BranchList.
 *
 * @author A.Arnold 01.10.2018
 */
public class BranchListTableModel extends AbstractTableModel implements IDiscardable {

    private final Disposable branchesDisposable;
    private final EBranchType branchType;
    private List<IBranch> branches = Collections.emptyList();

    /**
     * @param pBranches   The List of Branches to show
     * @param pBranchType The BranchType for the Branch
     */
    public BranchListTableModel(Observable<List<IBranch>> pBranches, @NotNull EBranchType pBranchType) {
        branchType = pBranchType;
        branchesDisposable = pBranches.subscribe(pBranchList -> {
            branches = pBranchList.stream()
                    .filter(pBranch -> pBranch.getName().startsWith(branchType.getSortKey(), 5))
                    .collect(Collectors.toList());
            fireTableDataChanged();
        });
    }

    @Override
    public int getRowCount() {
        return branches.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return branches.get(rowIndex).getName();
        }
        if (columnIndex == 1) {
            return branches.get(rowIndex).getId();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return branchType.getDisplayName();
        }
        if (column == 1) {
            return "branchID";
        }
        return null;
    }

    @Override
    public void discard() {
        branchesDisposable.dispose();
    }
}
