package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Offers a serviceExecutor and IDataModelUpdateListeners
 *
 * @author m.kaspera, 12.07.2019
 */
public abstract class BaseObservingTreeModel<T> extends DefaultTreeModel implements IDiscardable
{

  final File projectDirectory;
  final ExecutorService service = Executors.newSingleThreadExecutor();

  BaseObservingTreeModel(File pProjectDirectory)
  {
    super(null);
    projectDirectory = pProjectDirectory;
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

  abstract void _treeChanged(List<T> pNewElements, Runnable... pDoAfter);

  /**
   * searches the parentNode for a child whose nodeDescription equals the passed name
   *
   * @param pNode      Node that should be searched for the child
   * @param pChildName name of the child as String
   * @return the child with the passed name, or null if none with that name exists
   */
  @Nullable
  protected FileChangeTypeNode _getChildNode(@NotNull FileChangeTypeNode pNode, @NotNull String pChildName)
  {
    for (int index = pNode.getChildCount() - 1; index >= 0; index--)
    {
      FileChangeTypeNode child = (FileChangeTypeNode) pNode.getChildAt(index);
      if (child.getInfo() == null)
        continue;
      final String childDisplayName = child.getInfo().getNodeDescription();
      if (pChildName.equals(childDisplayName))
        return child;
    }
    return null;
  }

  /**
   * @param pDiffInfo DiffInfo for which to create the nodeDescription
   * @return formatted String used as nodeDescription in a FileChangeTypeNode
   */
  @NotNull
  protected String _getCommitNodeDescription(@NotNull IDiffInfo pDiffInfo)
  {
    return String.format("commit %8.8s", pDiffInfo.getParentCommit().getId());
  }

  /**
   * @param pDiffInfos List of DiffInfos whose changed files should be added up
   * @return List with all changed files, does not contain duplicates
   */
  @NotNull
  protected List<IFileChangeType> _getAllChangedFiles(@NotNull List<IDiffInfo> pDiffInfos)
  {
    if (pDiffInfos.size() == 1)
      return pDiffInfos.get(0).getChangedFiles();
    Set<IFileChangeType> allChangedFiles = new HashSet<>();
    pDiffInfos.forEach(pDiffInfo -> allChangedFiles.addAll(pDiffInfo.getChangedFiles()));
    return new ArrayList<>(allChangedFiles);
  }

  /**
   * removes all nodes one level below the parent that do not match any of the passed parentIds
   *
   * @param pList     list with the DiffInfos
   * @param pRootNode root node of the tree
   */
  @NotNull
  protected List<TreeUpdate> _removeOldCommitNodes(@NotNull List<IDiffInfo> pList, @NotNull FileChangeTypeNode pRootNode)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    for (int index = pRootNode.getChildCount() - 1; index >= 0; index--)
    {
      FileChangeTypeNode child = (FileChangeTypeNode) pRootNode.getChildAt(index);
      if (child.getInfo() == null)
      {
        treeUpdates.add(TreeUpdate.createRemove(child));
        continue;
      }
      final String childDisplayName = child.getInfo().getNodeDescription();
      if (pList.stream().noneMatch(pDiffInfo -> _getCommitNodeDescription(pDiffInfo).equals(childDisplayName)))
        treeUpdates.add(TreeUpdate.createRemove(child));
    }
    return treeUpdates;
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
}
