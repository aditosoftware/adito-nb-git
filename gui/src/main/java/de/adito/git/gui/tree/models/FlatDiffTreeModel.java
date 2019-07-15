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

import javax.swing.tree.MutableTreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author m.kaspera, 12.07.2019
 */
public class FlatDiffTreeModel extends BaseObservingTreeModel implements IDiscardable
{

  private final Disposable disposable;

  public FlatDiffTreeModel(@NotNull Observable<List<IDiffInfo>> pChangeList, @NotNull File pProjectDirectory)
  {
    super(pProjectDirectory);
    disposable = pChangeList.subscribe(this::_treeChanged);
  }

  private void _treeChanged(@NotNull List<IDiffInfo> pChangeList)
  {
    try
    {
      service.invokePriority(new TreeModelBackgroundUpdater<>(this, this::_calculateTree, pChangeList, _getDefaultComparator(), this::fireDataModelUpdated));
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

  @NotNull
  private List<TreeUpdate> _calculateTree(@NotNull List<IDiffInfo> pDiffInfos)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    FileChangeTypeNode rootNode = (FileChangeTypeNode) getRoot();
    if (rootNode == null)
    {
      rootNode = new FileChangeTypeNode(new FileChangeTypeNodeInfo(projectDirectory.getName(), projectDirectory, List.of()));
      setRoot(rootNode);
    }
    if (pDiffInfos.size() == 1)
    {
      treeUpdates.addAll(_handleSingleCommit(pDiffInfos, rootNode));
    }
    else
    {
      treeUpdates.addAll(_removeOldCommitNodes(pDiffInfos, rootNode));
      for (IDiffInfo diffInfo : pDiffInfos)
      {
        treeUpdates.addAll(_handleCommitNodes(rootNode, diffInfo));
      }
    }
    return treeUpdates;
  }

  @NotNull
  private List<TreeUpdate> _handleSingleCommit(@NotNull List<IDiffInfo> pDiffInfos, @NotNull FileChangeTypeNode pRootNode)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    List<IFileChangeType> changeTypes = pDiffInfos.get(0).getChangedFiles();
    for (int index = 0; index < pRootNode.getChildCount(); index++)
    {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedRuntimeException();
      List<IFileChangeType> members = ((FileChangeTypeNode) pRootNode.getChildAt(index)).getInfo().getMembers();
      if (members.isEmpty() || !changeTypes.contains(members.get(0)))
      {
        treeUpdates.add(TreeUpdate.createRemove((MutableTreeNode) pRootNode.getChildAt(index)));
      }
    }
    for (IFileChangeType changeType : changeTypes)
    {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedRuntimeException();
      if (!pRootNode.getInfo().getMembers().contains(changeType))
      {
        treeUpdates.add(TreeUpdate.createInsert(new FileChangeTypeNode(
            new FileChangeTypeNodeInfo(changeType.getFile().getName(), changeType.getFile(), List.of(changeType))), pRootNode, 0));
      }
    }
    pRootNode.getInfo().setMembers(changeTypes);
    return treeUpdates;
  }

  @NotNull
  private List<TreeUpdate> _handleCommitNodes(@NotNull FileChangeTypeNode pRootNode, @NotNull IDiffInfo diffInfo)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMap = _calculateFlatMap(diffInfo);
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

  @NotNull
  private HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _calculateFlatMap(@NotNull IDiffInfo pDiffInfo)
  {
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> groups = new HashMap<>();
    for (IFileChangeType changeType : pDiffInfo.getChangedFiles())
    {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedRuntimeException();
      File file = changeType.getFile();
      File parentFile = projectDirectory;
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
    }
    return groups;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
