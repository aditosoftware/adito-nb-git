package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.gui.concurrency.PriorityDroppingExecutor;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.text.Collator;
import java.util.*;

/**
 * Offers a serviceExecutor and IDataModelUpdateListeners
 *
 * @author m.kaspera, 12.07.2019
 */
public abstract class BaseObservingTreeModel extends DefaultTreeModel implements IDiscardable
{

  final File projectDirectory;
  final PriorityDroppingExecutor service = new PriorityDroppingExecutor();
  private final List<ObservingTreeModel.IDataModelUpdateListener> updateListeners = new ArrayList<>();

  BaseObservingTreeModel(File pProjectDirectory)
  {
    super(null);
    projectDirectory = pProjectDirectory;
  }

  /**
   * Queues the Task in the Single-thread executor of this class
   *
   * @param pRunnable Runnable to execute
   */
  public void invokeAfterComputations(@NotNull Runnable pRunnable)
  {
    registerDataModelUpdatedListener(new IDataModelUpdateListener()
    {
      @Override
      public void modelUpdated()
      {
        service.invokeAfterComputations(pRunnable);
        removeDataModelUpdateListener(this);
      }
    });
  }

  /**
   * registers a listener that is notified each time the model is fully updated (i.e. the update method completed a full run-through and was not interrupted. This means
   * the current state of the tree can be considered "valid"/up-to-date for the immideate future)
   *
   * @param pListener listener to be notified
   */
  public void registerDataModelUpdatedListener(ObservingTreeModel.IDataModelUpdateListener pListener)
  {
    updateListeners.add(pListener);
  }

  /**
   * removes a listener such that it gets no more updates
   *
   * @param pListener listener to be removed
   */
  public void removeDataModelUpdateListener(ObservingTreeModel.IDataModelUpdateListener pListener)
  {
    updateListeners.remove(pListener);
  }

  /**
   * notifies the listeners that the model did a full update cycle/the update function passed without it being aborted due to new data coming in
   */
  void fireDataModelUpdated()
  {
    for (int index = updateListeners.size() - 1; index >= 0; index--)
    {
      updateListeners.get(index).modelUpdated();
    }
  }

  /**
   * @return Comparator that orders according to the nodeInfo of a FileChangeTypeNode
   */
  @NotNull
  Comparator<TreeNode> _getDefaultComparator()
  {
    return Comparator.comparing(pO -> {
      FileChangeTypeNodeInfo nodeInfo = ((FileChangeTypeNode) pO).getInfo();
      if (nodeInfo != null)
        return nodeInfo.getNodeDescription();
      return "";
    }, Collator.getInstance());
  }

  /**
   * Defines an interface for objects interested in knowing when the dataModel was completely updated (completely because if the data changes during an update,
   * that update is aborted and a new update started)
   */
  public interface IDataModelUpdateListener extends EventListener
  {
    void modelUpdated();
  }

}
