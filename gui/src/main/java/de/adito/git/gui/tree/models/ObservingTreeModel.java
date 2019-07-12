package de.adito.git.gui.tree.models;

import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.concurrency.PriorityDroppingExecutor;
import de.adito.git.gui.tree.TreeUpdate;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.text.Collator;
import java.util.*;

/**
 * @author m.kaspera, 14.05.2019
 */
public class ObservingTreeModel extends DefaultTreeModel
{

  private final List<IDataModelUpdateListener> updateListeners = new ArrayList<>();
  File projectDirectory;
  PriorityDroppingExecutor service = new PriorityDroppingExecutor();

  ObservingTreeModel(@NotNull File pProjectDirectory)
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
    service.invokeAfterComputations(pRunnable);
  }

  /**
   * registers a listener that is notified each time the model is fully updated (i.e. the update method completed a full run-through and was not interrupted. This means
   * the current state of the tree can be considered "valid"/up-to-date for the immideate future)
   *
   * @param pListener listener to be notified
   */
  public void registerDataModelUpdatedListener(IDataModelUpdateListener pListener)
  {
    updateListeners.add(pListener);
  }

  /**
   * removes a listener such that it gets no more updates
   *
   * @param pListener listener to be removed
   */
  public void removeDataModelUpdateListener(IDataModelUpdateListener pListener)
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
   * @param pMap  Map that is an image of the tree
   * @param pNode node to update
   */
  @NotNull
  List<TreeUpdate> _updateTree(@NotNull HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, @NotNull FileChangeTypeNode pNode)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    if (Thread.currentThread().isInterrupted())
      throw new InterruptedRuntimeException();
    HashMap<File, FileChangeTypeNodeInfo> map = pNode.getInfo() == null ? null : pMap.get(pNode.getInfo().getNodeFile());
    if (map != null)
    {
      for (Map.Entry<File, FileChangeTypeNodeInfo> entry : map.entrySet())
      {
        FileChangeTypeNode childNode = FileChangeTypeNode.getChildNodeForFile(pNode, entry.getKey());
        if (childNode != null && childNode.getInfo() != null)
        {
          childNode.getInfo().setMembers(map.get(entry.getKey()).getMembers());
          // update assigned commit if necessary
          if (pNode.getAssignedCommit() != null && !pNode.getAssignedCommit().equals(childNode.getAssignedCommit()))
            childNode.setAssignedCommit(pNode.getAssignedCommit());
        }
        else
        {
          childNode = new FileChangeTypeNode(entry.getValue(), pNode.getAssignedCommit());
          treeUpdates.add(TreeUpdate.createInsert(childNode, pNode, 0));
        }
        treeUpdates.addAll(_updateTree(pMap, childNode));
      }
      for (TreeNode treeNode : Collections.list(pNode.children()))
      {
        FileChangeTypeNode fileChangeTypeNode = ((FileChangeTypeNode) treeNode);
        if (fileChangeTypeNode.getInfo() == null || !map.containsKey(fileChangeTypeNode.getInfo().getNodeFile()))
        {
          treeUpdates.add(TreeUpdate.createRemove((FileChangeTypeNode) treeNode));
        }
      }
    }
    return treeUpdates;
  }

  /**
   * calculates a HashMap with an inner HashMap from the list of FileChanges, the HashMap works similar to a tree: the file of the outer HashMap
   * is the parent, while the files from the inner HashMap are the children connected to that parent
   *
   * @param pList List of IFileChangeTypes
   * @return HashMap calculated from the list
   */
  @NotNull
  HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _calculateMap(@NotNull List<IFileChangeType> pList)
  {
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> groups = new HashMap<>();
    for (IFileChangeType changeType : pList)
    {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedRuntimeException();
      File file = changeType.getFile();
      while (file != null && !file.equals(projectDirectory.getParentFile()))
      {
        File parentFile = file.getParentFile();
        if (groups.containsKey(parentFile))
        {
          if (groups.get(parentFile).containsKey(file))
          {
            groups.get(parentFile).get(file).getMembers().add(changeType);
          }
          else
          {
            List<IFileChangeType> changeTypes = new ArrayList<>();
            changeTypes.add(changeType);
            groups.get(parentFile).put(file, new FileChangeTypeNodeInfo(file.getName(), file, changeTypes));
          }
        }
        else
        {
          HashMap<File, FileChangeTypeNodeInfo> innerMap = new HashMap<>();
          List<IFileChangeType> changeTypes = new ArrayList<>();
          changeTypes.add(changeType);
          innerMap.put(file, new FileChangeTypeNodeInfo(file.getName(), file, changeTypes));
          groups.put(parentFile, innerMap);
        }
        file = file.getParentFile();
      }
    }
    return groups;
  }

  /**
   * Removes all "redundant" nodes that only have one child by basically aggregating all "folder with only one subfolder"
   *
   * @param pMap   Map retrieved by calling _calculateMap with the fileChanges
   * @param pChangedFiles HashSet of all changed files, used to determine if a File is a file or directory
   * @param pStart startNode
   * @return HashMap that has the nodes with only one node as child collapsed to the parentNode
   */
  @NotNull
  HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _reduce(@NotNull HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, @NotNull HashSet<File> pChangedFiles,
                                                               @NotNull File pStart)
  {
    Set<File> iterableCopy = pMap.get(pStart) == null ? new HashSet<>() : new HashSet<>(pMap.get(pStart).keySet());
    for (File pChildFile : iterableCopy)
    {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedRuntimeException();
      if (pMap.containsKey(pChildFile) && pMap.get(pChildFile).keySet().size() == 1 && pChildFile.isDirectory() &&
          !pChangedFiles.contains(pMap.get(pChildFile).keySet().iterator().next()) && !pChildFile.equals(projectDirectory))
      {
        Map.Entry<File, FileChangeTypeNodeInfo> theSingleEntry = pMap.get(pChildFile).entrySet().iterator().next();
        File firstAvailableParent = _getFirstAvailableParent(pMap, pChildFile);
        if (firstAvailableParent != null)
        {
          pMap.get(firstAvailableParent).get(pChildFile).collapse(theSingleEntry.getValue());
          pMap.remove(pChildFile);
          FileChangeTypeNodeInfo removed = pMap.get(firstAvailableParent).remove(pChildFile);
          pMap.get(firstAvailableParent).put(theSingleEntry.getKey(), removed);
        }
        pChildFile = pStart;
      }
      if (pChildFile.isDirectory() && pMap.containsKey(pChildFile))
        pMap = _reduce(pMap, pChangedFiles, pChildFile);
    }
    return pMap;
  }

  /**
   * @param pMap  Map containing
   * @param pFile file for which the nearest parent in the map should be found
   * @return the next parent file that is contained in the map
   */
  @Nullable
  private File _getFirstAvailableParent(@NotNull HashMap<File, ?> pMap, @NotNull File pFile)
  {
    File parent = pFile.getParentFile();
    while (pMap.get(parent) == null)
    {
      parent = parent.getParentFile();
      if (parent == null)
        return null;
    }
    return parent;
  }

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
