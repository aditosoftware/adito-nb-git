package de.adito.git.gui.tree;

import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.models.BaseObservingTreeModel;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author m.kaspera, 12.06.2019
 */
public class TreeModelBackgroundUpdater<T> extends SwingWorker<List<TreeUpdate>, TreeUpdate>
{

  private final BaseObservingTreeModel treeModel;
  private final Function<List<T>, List<TreeUpdate>> workFunction;
  private final List<T> param;
  private final Comparator<TreeNode> comparator;
  private final Runnable[] doOnUpdateComplete;
  private static int factoryNumber = 0;
  private final int modelNumber;

  /**
   * @param pTreeModel          TreeModel that should be updated
   * @param pWorkFunction       function that is called to calculate the list of TreeUpdates
   * @param pParam              parameter that should be passed to the workFunction
   * @param pComparator         comparator used to compare and sort nodes
   * @param pDoOnUpdateComplete runnable that should be executed after the update (the whole update, including the EDT part) is done (can be used for listeners and such)
   */
  public TreeModelBackgroundUpdater(BaseObservingTreeModel pTreeModel, Function<List<T>, List<TreeUpdate>> pWorkFunction, List<T> pParam,
                                    Comparator<TreeNode> pComparator, Runnable... pDoOnUpdateComplete)
  {
    treeModel = pTreeModel;
    workFunction = pWorkFunction;
    param = pParam;
    comparator = pComparator;
    doOnUpdateComplete = pDoOnUpdateComplete;
    factoryNumber++;
    modelNumber = factoryNumber;
  }

  @Override
  @NonNull
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
        if (modelNumber != factoryNumber || isCancelled() || Thread.currentThread().isInterrupted())
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
          treeModel.removeNodeFromParent(update.getNode());
        }
      }
      Arrays.stream(doOnUpdateComplete).forEach(Runnable::run);
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

  private static int _findSortedIndex(@NonNull MutableTreeNode pParent, @NonNull MutableTreeNode pToInsert, @NonNull Comparator<TreeNode> pComparator)
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
