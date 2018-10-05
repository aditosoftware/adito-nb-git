package de.adito.git.gui.tableModels;

import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * BranchListTableModel is the AbstractTableModel for the BranchList.
 *
 * @author A.Arnold 01.10.2018
 */
public class BranchListTableModel extends AbstractTableModel {

    private List<IBranch> branches;
    private EBranchType branchType;
    private List<IBranch> filterBranchList;

    /**
     * @param pBranches   The List of Branches to show
     * @param pBranchType The BranchType for the Branch
     */
    public BranchListTableModel(List<IBranch> pBranches, @NotNull EBranchType pBranchType) {
        branches = pBranches;
        branchType = pBranchType;
        filterBranchList = new ArrayList<>();
        _refFilter();

    }

    @Override
    public int getRowCount() {
        return filterBranchList.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return filterBranchList.get(rowIndex).getName();
        }
        if (columnIndex == 1) {
            return filterBranchList.get(rowIndex).getId();
        }
        return null;
    }


    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return branchType.getDisplayName();
        }
        if (column == 1){
            return "branchID";
        }
        return null;
    }

    /**
     * add the branches to a new filtered list
     */
    private void _refFilter() {
        for (IBranch branch : branches) {
            if (branch.getName().startsWith(branchType.getSortKey(), 5)) {
                filterBranchList.add(branch);
            }
        }
    }
}
