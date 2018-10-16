package de.adito.git.gui.rxjava;

import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.util.function.Consumer;

/**
 * @author m.kaspera 16.10.2018
 */
public class ObservableTable extends JTable {

    private final Observable<Integer[]> selectedRowsObservable;

    public ObservableTable() {
        selectedRowsObservable = Observable.create(new _SelectedRowsObservable(getSelectionModel()))
                .startWith(new Integer[0]);
    }

    public Observable<Integer[]> selectedRows() {
        return selectedRowsObservable;
    }

    @NotNull
    private static Integer[] getSelectedRows(@NotNull ListSelectionModel pModel) {
        int iMin = pModel.getMinSelectionIndex();
        int iMax = pModel.getMaxSelectionIndex();

        if ((iMin == -1) || (iMax == -1)) {
            return new Integer[0];
        }

        Integer[] rvTmp = new Integer[1 + (iMax - iMin)];
        int n = 0;
        for (int i = iMin; i <= iMax; i++) {
            if (pModel.isSelectedIndex(i)) {
                rvTmp[n++] = i;
            }
        }
        Integer[] rv = new Integer[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }

    private static class _SelectedRowsObservable extends AbstractListenerObservable<ListSelectionListener, ListSelectionModel, Integer[]> {
        _SelectedRowsObservable(@NotNull ListSelectionModel pListenableValue) {
            super(pListenableValue);
        }

        @NotNull
        @Override
        protected ListSelectionListener registerListener(@NotNull ListSelectionModel pListenableValue, @NotNull Consumer<Integer[]> pOnNext) {
            ListSelectionListener listener = e -> pOnNext.accept(getSelectedRows(pListenableValue));
            pListenableValue.addListSelectionListener(listener);
            return listener;
        }

        @Override
        protected void removeListener(@NotNull ListSelectionModel pListenableValue, @NotNull ListSelectionListener pLISTENER) {
            pListenableValue.removeListSelectionListener(pLISTENER);
        }
    }
}
