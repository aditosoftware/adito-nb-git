package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.tree.DefaultTreeModel;
import java.io.File;
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
      FileChangeTypeNode.calculateChildren((FileChangeTypeNode) getRoot(), pList, projectDirectory);
    }
    else
    {
      FileChangeTypeNode root = (FileChangeTypeNode) getRoot();
      root.updateNode(pList, this);
      root.tryCollapse(this);
    }
  }
}
