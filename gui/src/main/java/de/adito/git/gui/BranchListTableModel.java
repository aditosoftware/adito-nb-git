package de.adito.git.gui;

import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class BranchListTableModel extends AbstractTableModel {

    private List<IBranch> branches;
    private EBranchType branchType;
    private List<IBranch> filterBranchList;

    BranchListTableModel(List<IBranch> pBranches, @NotNull EBranchType pBranchType) {
        branches = pBranches;
        branchType = pBranchType;
        filterBranchList = new ArrayList<>();
        refFilter();

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

    private void refFilter(){
        for (IBranch branch : branches) {
            if (branch.getName().startsWith(branchType.getSortKey(), 5)) {
                filterBranchList.add(branch);
            }
        }
    }
}
