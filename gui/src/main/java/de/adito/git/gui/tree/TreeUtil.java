package de.adito.git.gui.tree;

import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

/**
 * Utility class for methods commonly used with trees
 *
 * @author m.kaspera, 06.06.2019
 */
public class TreeUtil
{

  /**
   * Expands the nodes of the tree, but checks if the thread was interrupted and breaks the loop if it was interrupted
   *
   * @param pTree JTree to be expanded
   */
  public static void _expandTreeInterruptible(JTree pTree)
  {
    for (int i = 0; i < pTree.getRowCount(); i++)
    {
      if (Thread.currentThread().isInterrupted())
        break;
      pTree.expandRow(i);
    }
  }

  /**
   * create an Observable for all changes to the TreeModel
   *
   * @param pTreeModel TreeModel who should be observed for changes
   * @return Observable that fires the changes to the TreeModel
   */
  public static Observable<TreeModelEvent> getTreeModelChangeObservable(DefaultTreeModel pTreeModel)
  {
    return Observable.create(new _TreeModelChangeObservable(pTreeModel))
        .share()
        .subscribeWith(BehaviorSubject.create());
  }

  /**
   * Observable that fires the TreeModelEvents if the model of a tree changed
   */
  private static class _TreeModelChangeObservable extends AbstractListenerObservable<TreeModelListener, DefaultTreeModel, TreeModelEvent>
  {
    _TreeModelChangeObservable(@NotNull DefaultTreeModel pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected TreeModelListener registerListener(@NotNull DefaultTreeModel pDefaultTreeModel, @NotNull IFireable<TreeModelEvent> pIFireable)
    {
      TreeModelListener listener = new TreeModelListener()
      {
        @Override
        public void treeNodesChanged(TreeModelEvent e)
        {
          pIFireable.fireValueChanged(e);
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e)
        {
          pIFireable.fireValueChanged(e);
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e)
        {
          pIFireable.fireValueChanged(e);
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e)
        {
          // do not fire here, the tree itself did not change
        }
      };
      pDefaultTreeModel.addTreeModelListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull DefaultTreeModel pDefaultTreeModel, @NotNull TreeModelListener pTreeModelListener)
    {
      pDefaultTreeModel.removeTreeModelListener(pTreeModelListener);
    }
  }

}
