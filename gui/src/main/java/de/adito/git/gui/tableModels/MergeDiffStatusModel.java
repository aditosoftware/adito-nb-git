package de.adito.git.gui.tableModels;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author m.kaspera 25.10.2018
 */
public class MergeDiffStatusModel extends AbstractTableModel implements IDiscardable {

    private List<IMergeDiff> mergeDiffs;
    private final Disposable disposable;

    public MergeDiffStatusModel(Observable<List<IMergeDiff>> pMergeDiffObservable) {
        disposable = pMergeDiffObservable.subscribe(pMergeDiffs -> {
            mergeDiffs = pMergeDiffs;
            fireTableDataChanged();
        });
    }

    @Override
    public int getRowCount() {
        return mergeDiffs.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String path = mergeDiffs.get(rowIndex).getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW);
        if(columnIndex == 0){
            String[] pathFolders = path.split("/");
            return pathFolders[pathFolders.length - 1];
        } else {
            return path;
        }
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}
