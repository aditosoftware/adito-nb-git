package de.adito.git.gui.tree.models;

import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author m.kaspera, 14.05.2019
 */
class ObservingTreeModel extends DefaultTreeModel
{

  File projectDirectory;
  ExecutorService service = Executors.newSingleThreadExecutor();
  Future<?> updateFuture = null;

  ObservingTreeModel(File pProjectDirectory)
  {
    super(null);
    projectDirectory = pProjectDirectory;
  }

  /**
   * Queues the Task in the Single-thread executor of this class
   *
   * @param pRunnable Runnable to execute
   * @return Future of the submit method of the executor
   */
  public Future<?> invokeAfterComputations(Runnable pRunnable)
  {
    return service.submit(pRunnable);
  }

  /**
   * @param pMap  Map that is an image of the tree
   * @param pNode node to update
   */
  void _updateTree(HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, FileChangeTypeNode pNode)
  {
    if (Thread.interrupted())
      throw new InterruptedRuntimeException();
    HashMap<File, FileChangeTypeNodeInfo> map = pMap.get(pNode.getInfo().getNodeFile());
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
          insertNodeInto(childNode, pNode, 0);
        }
        _updateTree(pMap, childNode);
      }
      for (TreeNode childNode : Collections.list(pNode.children()))
      {
        if (!map.containsKey(((FileChangeTypeNode) childNode).getInfo().getNodeFile()))
        {
          removeNodeFromParent((FileChangeTypeNode) childNode);
        }
      }
    }
  }

  /**
   * calculates a HashMap with an inner HashMap from the list of FileChanges, the HashMap works similar to a tree: the file of the outer HashMap
   * is the parent, while the files from the inner HashMap are the children connected to that parent
   *
   * @param pList List of IFileChangeTypes
   * @return HashMap calculated from the list
   */
  HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _calculateMap(List<IFileChangeType> pList)
  {
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> groups = new HashMap<>();
    for (IFileChangeType changeType : pList)
    {
      File file = changeType.getFile();
      while (!file.equals(projectDirectory.getParentFile()))
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
   * @param pStart startNode
   * @return HashMap that has the nodes with only one node as child collapsed to the parentNode
   */
  HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _reduce(HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, File pStart)
  {
    if (Thread.interrupted())
      throw new InterruptedRuntimeException();
    Set<File> iterableCopy = pMap.get(pStart) == null ? new HashSet<>() : new HashSet<>(pMap.get(pStart).keySet());
    for (File pChildFile : iterableCopy)
    {
      if (pMap.containsKey(pChildFile) && pMap.get(pChildFile).keySet().size() == 1 && pChildFile.isDirectory() &&
          !pMap.get(pChildFile).keySet().iterator().next().isFile() && !pChildFile.equals(projectDirectory))
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
        pMap = _reduce(pMap, pChildFile);
    }
    return pMap;
  }

  /**
   * @param pMap  Map containing
   * @param pFile file for which the nearest parent in the map should be found
   * @return the next parent file that is contained in the map
   */
  @Nullable
  private File _getFirstAvailableParent(HashMap<File, ?> pMap, File pFile)
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

  Comparator<TreeNode> _getDefaultComparator()
  {
    return Comparator.comparing(pO -> {
      FileChangeTypeNodeInfo nodeInfo = ((FileChangeTypeNode) pO).getInfo();
      if (nodeInfo != null)
        return nodeInfo.getNodeDescription();
      return "";
    }, Collator.getInstance());
  }

}
