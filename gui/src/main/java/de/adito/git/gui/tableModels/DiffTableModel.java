package de.adito.git.gui.tableModels;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileDiff;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * TableModel for displaying a table of IFileDiffs
 *
 * @author m.kaspera 07.11.2018
 */
public class DiffTableModel extends AbstractTableModel {

    public static final String[] columnNames = {"Filepath", "Changetype"};
    private final List<IFileDiff> fileDiffs;

    public DiffTableModel(List<IFileDiff> pFileDiffs) {

        fileDiffs = pFileDiffs;
    }

    @Override
    public int getRowCount() {
        return fileDiffs.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return fileDiffs.get(rowIndex).getFilePath(EChangeSide.NEW);
        else if (columnIndex == 1)
            return fileDiffs.get(rowIndex).getChangeType();
        return null;
    }
}
