package de.adito.git.gui.tableModels;

import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;

/**
 * Model that describes the contents of the table in the StatusWindow
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusTableModel extends AbstractTableModel implements IDiscardable {

    public final static String FILE_NAME_COLUMN_NAME = "fileName";
    public final static String FILE_PATH_COLUMN_NAME = "filePath";
    public final static String CHANGE_TYPE_COLUMN_NAME = "changeType";
    public static final String[] columnNames = {FILE_NAME_COLUMN_NAME, FILE_PATH_COLUMN_NAME, CHANGE_TYPE_COLUMN_NAME};

    private IFileStatus status;
    private Disposable statusDisposable;

    public StatusTableModel(Observable<IFileStatus> pStatusObservable) {
        statusDisposable = pStatusObservable.subscribe(pStatus -> {
            status = pStatus;
            fireTableDataChanged();
        });
    }

    @Override
    public int findColumn(String columnName) {
        for (int index = 0; index < columnNames.length; index++) {
            if (columnNames[index].equals(columnName))
                return index;
        }
        return -1;
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
    public String getColumnName(int column) {
        return StatusTableModel.columnNames[column];
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
        // Columns : | filename | filepath     | changetype |
        // example:  | file     | example/file | modified   |

        if (columnIndex == findColumn(FILE_NAME_COLUMN_NAME))
            return status.getUncommitted().get(rowIndex).getFile().getName();
        else if (columnIndex == findColumn(FILE_PATH_COLUMN_NAME))
            return status.getUncommitted().get(rowIndex).getFile().getPath();
        else if (columnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
            return status.getUncommitted().get(rowIndex).getChangeType();
        else
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void discard() {
        statusDisposable.dispose();
    }
}
