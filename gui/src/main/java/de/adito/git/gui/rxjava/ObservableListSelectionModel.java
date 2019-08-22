package de.adito.git.gui.rxjava;

import de.adito.git.api.IDiscardable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * @author a.arnold, 14.11.2018
 */
public class ObservableListSelectionModel implements ListSelectionModel, IDiscardable
{

  private final Observable<Integer[]> selectedRowsObservable;
  private final ListSelectionModel delegate;
  private final CompositeDisposable disposables = new CompositeDisposable();

  public ObservableListSelectionModel(@NotNull ListSelectionModel pDelegate)
  {
    delegate = pDelegate;
    selectedRowsObservable = Observable.create(new _SelectedRowsObservable(this))
        .startWith(new Integer[0])
        .replay(1)
        .autoConnect(0, disposables::add);
  }

  @Override
  public void setSelectionInterval(int pIndex0, int pIndex1)
  {
    delegate.setSelectionInterval(pIndex0, pIndex1);
  }

  @Override
  public void addSelectionInterval(int pIndex0, int pIndex1)
  {
    delegate.addSelectionInterval(pIndex0, pIndex1);
  }

  @Override
  public void removeSelectionInterval(int pIndex0, int pIndex1)
  {
    delegate.removeSelectionInterval(pIndex0, pIndex1);
  }

  @Override
  public int getMinSelectionIndex()
  {
    return delegate.getMinSelectionIndex();
  }

  @Override
  public int getMaxSelectionIndex()
  {
    return delegate.getMaxSelectionIndex();
  }

  @Override
  public boolean isSelectedIndex(int pIndex)
  {
    return delegate.isSelectedIndex(pIndex);
  }

  @Override
  public int getAnchorSelectionIndex()
  {
    return delegate.getAnchorSelectionIndex();
  }

  @Override
  public void setAnchorSelectionIndex(int pIndex)
  {
    delegate.setAnchorSelectionIndex(pIndex);
  }

  @Override
  public int getLeadSelectionIndex()
  {
    return delegate.getLeadSelectionIndex();
  }

  @Override
  public void setLeadSelectionIndex(int pIndex)
  {
    delegate.setLeadSelectionIndex(pIndex);
  }

  @Override
  public void clearSelection()
  {
    delegate.clearSelection();
  }

  @Override
  public boolean isSelectionEmpty()
  {
    return delegate.isSelectionEmpty();
  }

  @Override
  public void insertIndexInterval(int pIndex, int pLength, boolean pBefore)
  {
    delegate.insertIndexInterval(pIndex, pLength, pBefore);
  }

  @Override
  public void removeIndexInterval(int pIndex0, int pIndex1)
  {
    delegate.removeIndexInterval(pIndex0, pIndex1);
  }

  @Override
  public void setValueIsAdjusting(boolean pValueIsAdjusting)
  {
    delegate.setValueIsAdjusting(pValueIsAdjusting);
  }

  @Override
  public boolean getValueIsAdjusting()
  {
    return delegate.getValueIsAdjusting();
  }

  @Override
  public void setSelectionMode(int pSelectionMode)
  {
    delegate.setSelectionMode(pSelectionMode);
  }

  @Override
  public int getSelectionMode()
  {
    return delegate.getSelectionMode();
  }

  @Override
  public void addListSelectionListener(ListSelectionListener pX)
  {
    delegate.addListSelectionListener(pX);
  }

  @Override
  public void removeListSelectionListener(ListSelectionListener pX)
  {
    delegate.removeListSelectionListener(pX);
  }

  public Observable<Integer[]> selectedRows()
  {
    return selectedRowsObservable
        .observeOn(Schedulers.computation());
  }

  @Override
  public void discard()
  {
    disposables.clear();
  }

  private static class _SelectedRowsObservable extends AbstractListenerObservable<ListSelectionListener, ListSelectionModel, Integer[]>
  {
    _SelectedRowsObservable(@NotNull ListSelectionModel pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected ListSelectionListener registerListener(@NotNull ListSelectionModel pListSelectionModel, @NotNull IFireable<Integer[]> pIFireable)
    {
      ListSelectionListener listener = e -> {
        if (!e.getValueIsAdjusting())
          pIFireable.fireValueChanged(_getSelectedRows(pListSelectionModel));
      };
      pListSelectionModel.addListSelectionListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull ListSelectionModel pListenableValue, @NotNull ListSelectionListener pLISTENER)
    {
      pListenableValue.removeListSelectionListener(pLISTENER);
    }

    @NotNull
    private static Integer[] _getSelectedRows(@NotNull ListSelectionModel pModel)
    {
      int iMin = pModel.getMinSelectionIndex();
      int iMax = pModel.getMaxSelectionIndex();

      if ((iMin == -1) || (iMax == -1))
      {
        return new Integer[0];
      }

      Integer[] rvTmp = new Integer[1 + (iMax - iMin)];
      int n = 0;
      for (int i = iMin; i <= iMax; i++)
      {
        if (pModel.isSelectedIndex(i))
        {
          rvTmp[n++] = i;
        }
      }
      Integer[] rv = new Integer[n];
      System.arraycopy(rvTmp, 0, rv, 0, n);
      return rv;
    }
  }
}
