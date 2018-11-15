package de.adito.git.gui.rxjava;

import de.adito.git.impl.rxjava.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.util.function.Consumer;

/**
 * @author a.arnold, 14.11.2018
 */
public class ObservableListSelectionModel implements ListSelectionModel {

    private final Observable<Integer[]> selectedRowsObservable;
    private final ListSelectionModel delegate;

    public ObservableListSelectionModel(ListSelectionModel pDelegate) {
        delegate = pDelegate;
        selectedRowsObservable = Observable.create(new _SelectedRowsObservable(this))
                .startWith(new Integer[0])
                .share()
                .subscribeWith(BehaviorSubject.create());
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        delegate.setSelectionInterval(index0, index1);
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        delegate.addSelectionInterval(index0, index1);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
        delegate.removeSelectionInterval(index0, index1);
    }

    @Override
    public int getMinSelectionIndex() {
        return delegate.getMinSelectionIndex();
    }

    @Override
    public int getMaxSelectionIndex() {
        return delegate.getMaxSelectionIndex();
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return delegate.isSelectedIndex(index);
    }

    @Override
    public int getAnchorSelectionIndex() {
        return delegate.getAnchorSelectionIndex();
    }

    @Override
    public void setAnchorSelectionIndex(int index) {
        delegate.setAnchorSelectionIndex(index);
    }

    @Override
    public int getLeadSelectionIndex() {
        return delegate.getLeadSelectionIndex();
    }

    @Override
    public void setLeadSelectionIndex(int index) {
        delegate.setLeadSelectionIndex(index);
    }

    @Override
    public void clearSelection() {
        delegate.clearSelection();
    }

    @Override
    public boolean isSelectionEmpty() {
        return delegate.isSelectionEmpty();
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        delegate.insertIndexInterval(index, length, before);
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
        delegate.removeIndexInterval(index0, index1);
    }

    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        delegate.setValueIsAdjusting(valueIsAdjusting);
    }

    @Override
    public boolean getValueIsAdjusting() {
        return delegate.getValueIsAdjusting();
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        delegate.setSelectionMode(selectionMode);
    }

    @Override
    public int getSelectionMode() {
        return delegate.getSelectionMode();
    }

    @Override
    public void addListSelectionListener(ListSelectionListener x) {
        delegate.addListSelectionListener(x);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener x) {
        delegate.removeListSelectionListener(x);
    }

    public Observable<Integer[]> selectedRows() {
        return selectedRowsObservable;
    }

    private static class _SelectedRowsObservable extends AbstractListenerObservable<ListSelectionListener, ListSelectionModel, Integer[]> {
        _SelectedRowsObservable(@NotNull ListSelectionModel pListenableValue) {
            super(pListenableValue);
        }

        @NotNull
        @Override
        protected ListSelectionListener registerListener(@NotNull ListSelectionModel pListenableValue, @NotNull Consumer<Integer[]> pOnNext) {
            ListSelectionListener listener = e -> {
                if (!e.getValueIsAdjusting())
                    pOnNext.accept(getSelectedRows(pListenableValue));
            };
            pListenableValue.addListSelectionListener(listener);
            return listener;
        }

        @Override
        protected void removeListener(@NotNull ListSelectionModel pListenableValue, @NotNull ListSelectionListener pLISTENER) {
            pListenableValue.removeListSelectionListener(pLISTENER);
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
    }
}
