package de.adito.git.gui;

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
     *
     * @param pBranches The List of Branches to show
     * @param pBranchType The BranchType for the Branch
     */
    BranchListTableModel(List<IBranch> pBranches, @NotNull EBranchType pBranchType) {
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
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return filterBranchList.get(rowIndex).getName();
    }

    @Override
    public String getColumnName(int column) {
        return branchType.getDisplayName();
    }

    /**
     * add the branches to a new filtered list
     */
    private void _refFilter(){
        for (IBranch branch : branches) {
            if (branch.getName().startsWith(branchType.getSortKey(), 5)) {
                filterBranchList.add(branch);
            }
        }
    }
}
