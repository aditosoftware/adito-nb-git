package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.TreeModelBackgroundUpdater;
import de.adito.git.gui.tree.TreeUpdate;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;

/**
 * Model for the Tree that displays the changed files
 *
 * @author m.kaspera, 22.02.2019
 */
public class DiffTreeModel extends ObservingTreeModel implements IDiscardable
{

  private final Disposable disposable;
  private Comparator<TreeNode> comparator = _getDefaultComparator();

  public DiffTreeModel(@NotNull Observable<List<IDiffInfo>> pChangeList, @NotNull File pProjectDirectory)
  {
    super(pProjectDirectory);
    disposable = pChangeList.subscribe(this::_treeChanged);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    service.shutdown();
  }

  /**
   * update the tree so that the information fits the one passed in the pList
   *
   * @param pList list of DiffInfos to display
   */
  @NotNull
  private List<TreeUpdate> _calculateTree(@NotNull List<IDiffInfo> pList)
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
  @NotNull
  private List<TreeUpdate> _updateNodes(@NotNull List<IDiffInfo> pList, @Nullable FileChangeTypeNode pRootNode)
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
      fileHashMap = _reduce(fileHashMap, projectDirectory.getParentFile());
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
  private void _treeChanged(@NotNull List<IDiffInfo> pList)
  {
    try
    {
      service.invokePriority(new TreeModelBackgroundUpdater<>(this, this::_calculateTree, pList, comparator));
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
   * removes all nodes one level below the parent that do not match any of the passed parentIds
   *
   * @param pList     list with the DiffInfos
   * @param pRootNode root node of the tree
   */
  @NotNull
  private List<TreeUpdate> _removeOldCommitNodes(@NotNull List<IDiffInfo> pList, @NotNull FileChangeTypeNode pRootNode)
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
   * creates or updates the node for the diffInfo/the parentCommit in the diffInfo
   *
   * @param pRootNode the root node of the tree
   * @param diffInfo  DiffInfo for which a commit node should be created
   */
  @NotNull
  private List<TreeUpdate> _handleCommitNode(@NotNull FileChangeTypeNode pRootNode, @NotNull IDiffInfo diffInfo)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMap = _calculateMap(diffInfo.getChangedFiles());
    fileHashMap = _reduce(fileHashMap, projectDirectory.getParentFile());
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

  /**
   * searches the parentNode for a child whose nodeDescription equals the passed name
   *
   * @param pNode      Node that should be searched for the child
   * @param pChildName name of the child as String
   * @return the child with the passed name, or null if none with that name exists
   */
  @Nullable
  private FileChangeTypeNode _getChildNode(@NotNull FileChangeTypeNode pNode, @NotNull String pChildName)
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
  private String _getCommitNodeDescription(@NotNull IDiffInfo pDiffInfo)
  {
    return String.format("commit %8.8s", pDiffInfo.getParentCommit().getId());
  }

  /**
   * @param pDiffInfos List of DiffInfos whose changed files should be added up
   * @return List with all changed files, does not contain duplicates
   */
  private @NotNull List<IFileChangeType> _getAllChangedFiles(@NotNull List<IDiffInfo> pDiffInfos)
  {
    if (pDiffInfos.size() == 1)
      return pDiffInfos.get(0).getChangedFiles();
    Set<IFileChangeType> allChangedFiles = new HashSet<>();
    pDiffInfos.forEach(pDiffInfo -> allChangedFiles.addAll(pDiffInfo.getChangedFiles()));
    return new ArrayList<>(allChangedFiles);
  }
}
