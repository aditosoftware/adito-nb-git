package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;

/**
 * Model for the Tree that displays the changed files
 *
 * @author m.kaspera, 22.02.2019
 */
public class StatusTreeModel extends ObservingTreeModel implements IDiscardable
{

  private final Disposable disposable;
  private Comparator<TreeNode> comparator = _getDefaultComparator();

  public StatusTreeModel(Observable<List<IFileChangeType>> pChangeList, File pProjectDirectory)
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

  private void _calculateTree(List<IFileChangeType> pList)
  {
    FileChangeTypeNode rootNode = (FileChangeTypeNode) getRoot();
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> fileHashMap = _calculateMap(pList);
    if (!fileHashMap.isEmpty())
    {
      fileHashMap = _reduce(fileHashMap, projectDirectory.getParentFile());
      if (rootNode == null)
      {
        setRoot(new FileChangeTypeNode(fileHashMap.get(projectDirectory.getParentFile()).get(projectDirectory)));
        rootNode = (FileChangeTypeNode) getRoot();
        reload();
      }
      FileChangeTypeNodeInfo rootInfo = rootNode.getInfo();
      if (rootInfo != null)
        rootInfo.setMembers(pList);
      _updateTree(fileHashMap, rootNode);
      rootNode.sort(comparator, this);
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
          removeNodeFromParent((FileChangeTypeNode) treeNode);
        }
      }
    }
  }

  private void _treeChanged(List<IFileChangeType> pList)
  {
    if (updateFuture != null && !updateFuture.isDone())
    {
      updateFuture.cancel(true);
    }
    updateFuture = invokeAfterComputations(() -> {
      try
      {
        _calculateTree(pList);
      }
      catch (InterruptedRuntimeException pE)
      {
        // do nothing, exception is thrown to cancel the current computation
      }
    });
  }
}
