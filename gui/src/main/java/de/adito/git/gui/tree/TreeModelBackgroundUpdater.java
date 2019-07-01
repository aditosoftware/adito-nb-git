package de.adito.git.gui.tree;

import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.models.ObservingTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author m.kaspera, 12.06.2019
 */
public class TreeModelBackgroundUpdater<T> extends SwingWorker<List<TreeUpdate>, TreeUpdate>
{

  private final ObservingTreeModel treeModel;
  private final Function<List<T>, List<TreeUpdate>> workFunction;
  private final List<T> param;
  private final Comparator<TreeNode> comparator;

  public TreeModelBackgroundUpdater(ObservingTreeModel pTreeModel, Function<List<T>, List<TreeUpdate>> pWorkFunction, List<T> pParam,
                                    Comparator<TreeNode> pComparator)
  {
    treeModel = pTreeModel;
    workFunction = pWorkFunction;
    param = pParam;
    comparator = pComparator;
  }

  @Override
  @NotNull
  protected List<TreeUpdate> doInBackground()
  {
    try
    {
      return workFunction.apply(param);
    }
    catch (InterruptedRuntimeException pE)
    {
      // computation cancelled, return empty list because the result is void anyways
      return List.of();
    }
  }

  @Override
  protected void done()
  {
    List<TreeUpdate> treeUpdates;
    try
    {
      treeUpdates = get();
      for (TreeUpdate update : treeUpdates)
      {
        if (isCancelled() || Thread.currentThread().isInterrupted())
          return;
        if (update.getType() == TreeUpdate.TYPE.INSERT)
        {
          treeModel.insertNodeInto(update.getNode(), update.getParent(), Math.min(update.getParent().getChildCount(),
                                                                                  _findSortedIndex(update.getParent(), update.getNode(), comparator)));
        }
        else if (update.getType() == TreeUpdate.TYPE.ROOT)
        {
          treeModel.setRoot(update.getNode());
          treeModel.reload();
        }
        else if (update.getType() == TreeUpdate.TYPE.REMOVE && update.getNode().getParent() != null)
        {
          TreeNode toReload = update.getNode().getParent();
          treeModel.removeNodeFromParent(update.getNode());
          treeModel.reload(toReload);
        }
      }
    }
    catch (InterruptedRuntimeException pE)
    {
      // nothing, thread was interrupted and should end its computation
    }
    catch (InterruptedException | ExecutionException pE)
    {
      Thread.currentThread().interrupt();
      throw new RuntimeException(pE);
    }
  }

  private static int _findSortedIndex(@NotNull MutableTreeNode pParent, @NotNull MutableTreeNode pToInsert, @NotNull Comparator<TreeNode> pComparator)
  {
    for (int index = 0; index < pParent.getChildCount(); index++)
    {
      if (pComparator.compare(pToInsert, pParent.getChildAt(index)) < 0)
      {
        return index;
      }
    }
    return pParent.getChildCount();
  }
}