package de.adito.git.gui.rxjava;

import de.adito.git.api.IDiscardable;
import de.adito.util.reactive.AbstractListenerObservable;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import lombok.NonNull;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.beans.PropertyChangeListener;

/**
 * @author m.kaspera, 20.02.2019
 */
public class ObservableTreeSelectionModel implements TreeSelectionModel, IDiscardable
{

  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final TreeSelectionModel delegate;


  public ObservableTreeSelectionModel(TreeSelectionModel pDelegate)
  {
    delegate = pDelegate;
    disposables.add(new ObservableCacheDisposable(observableCache));
  }


  public Observable<TreePath[]> getSelectedPaths()
  {
    return observableCache.calculateParallel("selectedPaths", () -> Observable.create(new _TreeSelectionObservable(this))
        .startWithItem(delegate.getSelectionPaths()));
  }

  @Override
  public void setSelectionMode(int pMode)
  {
    delegate.setSelectionMode(pMode);
  }

  @Override
  public int getSelectionMode()
  {
    return delegate.getSelectionMode();
  }

  @Override
  public void setSelectionPath(TreePath pPath)
  {
    delegate.setSelectionPath(pPath);
  }

  @Override
  public void setSelectionPaths(TreePath[] pPaths)
  {
    delegate.setSelectionPaths(pPaths);
  }

  @Override
  public void addSelectionPath(TreePath pPath)
  {
    delegate.addSelectionPath(pPath);
  }

  @Override
  public void addSelectionPaths(TreePath[] pPaths)
  {
    delegate.addSelectionPaths(pPaths);
  }

  @Override
  public void removeSelectionPath(TreePath pPath)
  {
    delegate.removeSelectionPath(pPath);
  }

  @Override
  public void removeSelectionPaths(TreePath[] pPaths)
  {
    delegate.removeSelectionPaths(pPaths);
  }

  @Override
  public TreePath getSelectionPath()
  {
    return delegate.getSelectionPath();
  }

  @Override
  public TreePath[] getSelectionPaths()
  {
    return delegate.getSelectionPaths();
  }

  @Override
  public int getSelectionCount()
  {
    return delegate.getSelectionCount();
  }

  @Override
  public boolean isPathSelected(TreePath pPath)
  {
    return delegate.isPathSelected(pPath);
  }

  @Override
  public boolean isSelectionEmpty()
  {
    return delegate.isSelectionEmpty();
  }

  @Override
  public void clearSelection()
  {
    delegate.clearSelection();
  }

  @Override
  public void setRowMapper(RowMapper pNewMapper)
  {
    delegate.setRowMapper(pNewMapper);
  }

  @Override
  public RowMapper getRowMapper()
  {
    return delegate.getRowMapper();
  }

  @Override
  public int[] getSelectionRows()
  {
    return delegate.getSelectionRows();
  }

  @Override
  public int getMinSelectionRow()
  {
    return delegate.getMinSelectionRow();
  }

  @Override
  public int getMaxSelectionRow()
  {
    return delegate.getMaxSelectionRow();
  }

  @Override
  public boolean isRowSelected(int pRow)
  {
    return delegate.isRowSelected(pRow);
  }

  @Override
  public void resetRowSelection()
  {
    delegate.resetRowSelection();
  }

  @Override
  public int getLeadSelectionRow()
  {
    return delegate.getLeadSelectionRow();
  }

  @Override
  public TreePath getLeadSelectionPath()
  {
    return delegate.getLeadSelectionPath();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener pListener)
  {
    delegate.addPropertyChangeListener(pListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener pListener)
  {
    delegate.removePropertyChangeListener(pListener);
  }

  @Override
  public void addTreeSelectionListener(TreeSelectionListener pX)
  {
    delegate.addTreeSelectionListener(pX);
  }

  @Override
  public void removeTreeSelectionListener(TreeSelectionListener pX)
  {
    delegate.removeTreeSelectionListener(pX);
  }

  @Override
  public void discard()
  {
    disposables.dispose();
  }

  private static class _TreeSelectionObservable extends AbstractListenerObservable<TreeSelectionListener, TreeSelectionModel, TreePath[]>
  {

    _TreeSelectionObservable(@NonNull TreeSelectionModel pListenableValue)
    {
      super(pListenableValue);
    }

    @NonNull
    @Override
    protected TreeSelectionListener registerListener(@NonNull TreeSelectionModel pTreeSelectionModel, @NonNull IFireable<TreePath[]> pIFireable)
    {
      TreeSelectionListener listener = e -> pIFireable.fireValueChanged(pTreeSelectionModel.getSelectionPaths());
      pTreeSelectionModel.addTreeSelectionListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NonNull TreeSelectionModel pTreeSelectionModel, @NonNull TreeSelectionListener pTreeSelectionListener)
    {
      pTreeSelectionModel.removeTreeSelectionListener(pTreeSelectionListener);
    }
  }
}
