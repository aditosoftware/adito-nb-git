package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.TreeModelBackgroundUpdater;
import de.adito.git.gui.tree.TreeUpdate;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Model for the Tree that displays the changed files
 *
 * @author m.kaspera, 22.02.2019
 */
public class StatusTreeModel extends ObservingTreeModel implements IDiscardable
{

  private final Disposable disposable;
  private Comparator<TreeNode> comparator = _getDefaultComparator();

  public StatusTreeModel(@NotNull Observable<List<IFileChangeType>> pChangeList, @NotNull File pProjectDirectory)
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

  @NotNull
  private List<TreeUpdate> _calculateTree(@NotNull List<IFileChangeType> pList)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    FileChangeTypeNode rootNode = (FileChangeTypeNode) getRoot();
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMap = _calculateMap(pList);
    if (!fileHashMap.isEmpty())
    {
      fileHashMap = _reduce(fileHashMap, pList.stream().map(IFileChangeType::getFile).collect(Collectors.toCollection(HashSet::new)), projectDirectory.getParentFile());
      if (rootNode == null)
      {
        rootNode = new FileChangeTypeNode(fileHashMap.get(projectDirectory.getParentFile()).get(projectDirectory));
        treeUpdates.add(TreeUpdate.createRoot(rootNode));
      }
      FileChangeTypeNodeInfo rootInfo = rootNode.getInfo();
      if (rootInfo != null)
        rootInfo.setMembers(pList);
      treeUpdates.addAll(_updateTree(fileHashMap, rootNode));
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

  private void _treeChanged(@NotNull List<IFileChangeType> pList)
  {
    try
    {
      service.invokePriority(new TreeModelBackgroundUpdater<>(this, this::_calculateTree, pList, comparator, this::fireDataModelUpdated));
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
}
