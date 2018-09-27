package de.adito.git;

import de.adito.git.api.data.IFileStatus;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;

/**
 * Model that describes the contents of the table in the StatusWindow
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusTableModel extends AbstractTableModel {

    static final String[] columnNames = {"Filename", "Filepath", "Changetype"};

    private IFileStatus status;

    StatusTableModel(IFileStatus pStatus) {
        status = pStatus;
    }

    @Override
    public int getRowCount() {
        return status.getUncommitted().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Nullable
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return status.getUncommitted().get(rowIndex).getFile().getName();
            case 1:
                return status.getUncommitted().get(rowIndex).getFile().getPath();
            case 2:
                return status.getUncommitted().get(rowIndex).getChangeType().toString();
            default:
                return null;
        }

    }
}
