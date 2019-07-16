package de.adito.git.gui.tree;

import de.adito.git.gui.tree.models.BaseObservingTreeModel;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;

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
   * create an Observable that observes the changes to the TreeModel of the given Tree. If the model is switched, the Observable from this method switches to observing
   * the new model and continues to fire on changes to the (now new) data model
   *
   * @param pTree Tree whose model should be observed for changes
   * @return Observable that fires on changes to the treeModel, even if that model is changed
   */
  public static Observable<TreeModelEvent> getTreeModelChangeObservable(JTree pTree)
  {
    return Observable.create(new _TreeModelObservable(pTree)).switchMap(TreeUtil::getTreeModelChangeObservable);
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

  /**
   * Observable taht fires if the model of a tree is changed
   */
  private static class _TreeModelObservable extends AbstractListenerObservable<PropertyChangeListener, JTree, BaseObservingTreeModel>
  {

    _TreeModelObservable(@NotNull JTree pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected PropertyChangeListener registerListener(@NotNull JTree pTree, @NotNull IFireable<BaseObservingTreeModel> pIFireable)
    {
      PropertyChangeListener listener = e -> {
        if (pTree.getModel() instanceof BaseObservingTreeModel)
          pIFireable.fireValueChanged((BaseObservingTreeModel) pTree.getModel());
      };
      pTree.addPropertyChangeListener("model", listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull JTree pTree, @NotNull PropertyChangeListener pPropertyChangeListener)
    {
      pTree.removePropertyChangeListener(pPropertyChangeListener);
    }
  }

  /**
   * Backwards extracts the full name of a tag from the TreePath its leaf node has
   *
   * @param pTreePath TreePath of the selected node
   * @return full name of the tag of the selected node
   */
  @NotNull
  public static Path _pathFromTreePath(@NotNull TreePath pTreePath)
  {
    // PathCount == 1: only root, the root label does not factor into the tag path
    if (pTreePath.getPathCount() > 1)
    {
      String[] pathComponents = new String[pTreePath.getPathCount() - 2];
      // index starts at 2 because the first node is the root node, which does not belong in the tag path, and the second component is passed seperately
      for (int index = 2; index < pTreePath.getPathCount(); index++)
      {
        // the component retrieved from the treePath is offset by 2 because the first real path element is passed seperately in Paths.get, and root is moot ;)
        pathComponents[index - 2] = pTreePath.getPathComponent(index).toString();
      }
      return Paths.get(pTreePath.getPathComponent(1).toString(), pathComponents);
    }
    return Paths.get("");
  }

}
