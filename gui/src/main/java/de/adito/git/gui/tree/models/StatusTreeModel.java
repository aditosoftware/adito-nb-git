package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;

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
  }

  private void _calculateTree(List<IFileChangeType> pList)
  {
    if (getRoot() == null)
    {
      setRoot(new FileChangeTypeNode(new FileChangeTypeNodeInfo(projectDirectory.getName(), projectDirectory, pList)));
      FileChangeTypeNode.calculateChildren((FileChangeTypeNode) getRoot(), this, pList, projectDirectory);
    }
    else
    {
      FileChangeTypeNode root = (FileChangeTypeNode) getRoot();
      root.updateNode(pList, this);
      root.tryCollapse(this);
      root.sort(comparator, this);
    }
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
