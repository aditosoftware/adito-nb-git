package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
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
 * Model for the Tree that displays the changed files
 *
 * @author m.kaspera, 22.02.2019
 */
public class StatusTreeModel extends DefaultTreeModel implements IDiscardable
{

  private final File projectDirectory;
  private final Disposable disposable;
  private Comparator<TreeNode> comparator = _getDefaultComparator();
  private ExecutorService service = Executors.newSingleThreadExecutor();
  private Future<?> updateFuture = null;

  public StatusTreeModel(Observable<List<IFileChangeType>> pChangeList, File pProjectDirectory)
  {
    super(null);
    projectDirectory = pProjectDirectory;
    disposable = pChangeList.subscribe(this::_calculateTree);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    service.shutdown();
  }

  /**
   * calculates a HashMap with an inner HashMap from the list of FileChanges, the HashMap works similar to a tree: the file of the outer HashMap
   * is the parent, while the files from the inner HashMap are the children connected to that parent
   *
   * @param pList List of IFileChangeTypes
   * @return HashMap calculated from the list
   */
  private HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _calculateMap(List<IFileChangeType> pList)
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
  private HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _reduce(HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, File pStart)
  {
    if (Thread.interrupted())
      throw new InterruptedRuntimeException();
    Set<File> iterableCopy = new HashSet<>(pMap.get(pStart).keySet());
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

  /**
   * @param pMap  Map that is an image of the tree
   * @param pNode node to update
   */
  private void _updateTree(HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, FileChangeTypeNode pNode)
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
        }
        else
        {
          childNode = new FileChangeTypeNode(entry.getValue());
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

  private void _calculateTree(List<IFileChangeType> pList)
  {

    if (updateFuture != null && !updateFuture.isDone())
    {
      updateFuture.cancel(true);
    }
    updateFuture = service.submit(() -> {
      try
      {
        HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMapHashMap = _calculateMap(pList);
        HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> reducedMap = _reduce(fileHashMapHashMap, projectDirectory.getParentFile());
        if (getRoot() == null)
        {
          setRoot(new FileChangeTypeNode(reducedMap.get(projectDirectory.getParentFile()).get(projectDirectory)));
        }
        FileChangeTypeNode rootNode = (FileChangeTypeNode) getRoot();
        FileChangeTypeNodeInfo rootInfo = rootNode.getInfo();
        if (rootInfo != null)
          rootInfo.setMembers(pList);
        _updateTree(reducedMap, rootNode);
        rootNode.sort(comparator, this);
      }
      catch (InterruptedRuntimeException pE)
      {
        // do nothing, exception is thrown to cancel the current computation
      }
    });
  }

  private Comparator<TreeNode> _getDefaultComparator()
  {
    return Comparator.comparing(pO -> {
      FileChangeTypeNodeInfo nodeInfo = ((FileChangeTypeNode) pO).getInfo();
      if (nodeInfo != null)
        return nodeInfo.getNodeDescription();
      return "";
    }, Collator.getInstance());
  }
}
