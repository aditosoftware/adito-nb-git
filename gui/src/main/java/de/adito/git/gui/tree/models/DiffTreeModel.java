package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.TreeModelBackgroundUpdater;
import de.adito.git.gui.tree.TreeUpdate;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Model for the Tree that displays the changed files
 *
 * @author m.kaspera, 22.02.2019
 */
public class DiffTreeModel extends ObservingTreeModel<IDiffInfo> implements IDiscardable
{

  private final Comparator<TreeNode> comparator = _getDefaultComparator();

  public DiffTreeModel(@NonNull File pProjectDirectory)
  {
    super(pProjectDirectory);
  }

  @Override
  public void discard()
  {
    service.shutdown();
  }

  /**
   * update the tree so that the information fits the one passed in the pList
   *
   * @param pList list of DiffInfos to display
   */
  @NonNull
  private List<TreeUpdate> _calculateTree(@NonNull List<IDiffInfo> pList)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    FileChangeTypeNode rootNode = (FileChangeTypeNode) getRoot();
    if (!pList.isEmpty() && !(pList.size() == 1 && pList.get(0).getChangedFiles().isEmpty()))
    {
      treeUpdates.addAll(_updateNodes(pList, rootNode));
    }
    else
    {
      if (rootNode != null)
      {
        FileChangeTypeNodeInfo rootInfo = rootNode.getInfo();
        if (rootInfo != null)
          rootInfo.setMembers(new ArrayList<>());
        for (TreeNode treeNode : Collections.list(rootNode.children()))
        {
          treeUpdates.add(TreeUpdate.createRemove((FileChangeTypeNode) treeNode));
        }
      }
    }
    return treeUpdates;
  }

  /**
   * makes sure the nodes are updated and have the same data as the pList, also sorts the nodes in the end
   *
   * @param pList     list of DiffInfos to display
   * @param pRootNode root node of the tree
   */
  @NonNull
  private List<TreeUpdate> _updateNodes(@NonNull List<IDiffInfo> pList, @Nullable FileChangeTypeNode pRootNode)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    List<IFileChangeType> allChangedFiles = _getAllChangedFiles(pList);
    if (pRootNode == null)
    {
      FileChangeTypeNode root = new FileChangeTypeNode(new FileChangeTypeNodeInfo(projectDirectory.getName(), projectDirectory, allChangedFiles));
      treeUpdates.add(TreeUpdate.createRoot(root));
      pRootNode = root;
    }
    FileChangeTypeNodeInfo rootInfo = pRootNode.getInfo();
    if (rootInfo != null)
      rootInfo.setMembers(allChangedFiles);
    if (pList.size() == 1)
    {
      HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMap = _calculateMap(pList.get(0).getChangedFiles());
      fileHashMap = _reduce(fileHashMap, pList.get(0).getChangedFiles().stream().map(IFileChangeType::getFile).collect(Collectors.toCollection(HashSet::new))
          , projectDirectory.getParentFile());
      pRootNode.setAssignedCommit(pList.get(0).getParentCommit());
      treeUpdates.addAll(_updateTree(fileHashMap, pRootNode));
    }
    else
    {
      treeUpdates.addAll(_removeOldCommitNodes(pList, pRootNode));
      for (IDiffInfo diffInfo : pList)
      {
        treeUpdates.addAll(_handleCommitNode(pRootNode, diffInfo));
      }
    }
    return treeUpdates;
  }

  /**
   * @param pList the up-to-date list of DiffInfos the tree should display
   */
  void _treeChanged(@NonNull List<IDiffInfo> pList, Runnable... pDoAfter)
  {
    try
    {
      service.submit(new TreeModelBackgroundUpdater<>(this, this::_calculateTree, pList, comparator, pDoAfter));
    }
    catch (InterruptedRuntimeException pE)
    {
      // do nothing, exception is thrown to cancel the current computation
    }
    catch (Exception pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * creates or updates the node for the diffInfo/the parentCommit in the diffInfo
   *
   * @param pRootNode the root node of the tree
   * @param diffInfo  DiffInfo for which a commit node should be created
   */
  @NonNull
  private List<TreeUpdate> _handleCommitNode(@NonNull FileChangeTypeNode pRootNode, @NonNull IDiffInfo diffInfo)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMap = _calculateMap(diffInfo.getChangedFiles());
    fileHashMap = _reduce(fileHashMap, diffInfo.getChangedFiles().stream().map(IFileChangeType::getFile).collect(Collectors.toCollection(HashSet::new)),
                          projectDirectory.getParentFile());
    FileChangeTypeNode commitInfoNode = _getChildNode(pRootNode, _getCommitNodeDescription(diffInfo));
    if (commitInfoNode == null)
    {
      commitInfoNode = new FileChangeTypeNode(
          new FileChangeTypeNodeInfo(_getCommitNodeDescription(diffInfo), projectDirectory, diffInfo.getChangedFiles()),
          diffInfo.getParentCommit());
      treeUpdates.add(TreeUpdate.createInsert(commitInfoNode, pRootNode, 0));
    }
    else if (commitInfoNode.getInfo() != null)
    {
      commitInfoNode.getInfo().setMembers(diffInfo.getChangedFiles());
    }
    treeUpdates.addAll(_updateTree(fileHashMap, commitInfoNode));
    return treeUpdates;
  }
}
