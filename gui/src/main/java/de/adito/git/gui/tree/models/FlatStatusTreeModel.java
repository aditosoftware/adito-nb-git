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

import javax.swing.tree.MutableTreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Model that arrays all IFileChangeTypes as direct children of root
 *
 * @author m.kaspera, 12.07.2019
 */
public class FlatStatusTreeModel extends BaseObservingTreeModel implements IDiscardable
{
  private final Disposable disposable;

  public FlatStatusTreeModel(@NotNull Observable<List<IFileChangeType>> pChangeList, @NotNull File pProjectDirectory)
  {
    super(pProjectDirectory);
    disposable = pChangeList.subscribe(this::_treeChanged);
  }

  private void _treeChanged(@NotNull List<IFileChangeType> pChangeList)
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

  private List<TreeUpdate> _calculateTree(@NotNull List<IFileChangeType> pChangeList)
  {
    List<TreeUpdate> treeUpdates = new ArrayList<>();
    FileChangeTypeNode rootNode = (FileChangeTypeNode) getRoot();
    if (rootNode == null)
    {
      rootNode = new FileChangeTypeNode(new FileChangeTypeNodeInfo(projectDirectory.getName(), projectDirectory, List.of()));
      setRoot(rootNode);
    }
    for (int index = 0; index < rootNode.getChildCount(); index++)
    {
      if (!pChangeList.contains(((FileChangeTypeNode) rootNode.getChildAt(index)).getInfo().getMembers().get(0)))
      {
        treeUpdates.add(TreeUpdate.createRemove((MutableTreeNode) rootNode.getChildAt(index)));
      }
    }
    for (IFileChangeType changeType : pChangeList)
    {
      if (!rootNode.getInfo().getMembers().contains(changeType))
      {
        treeUpdates.add(TreeUpdate.createInsert(new FileChangeTypeNode(new FileChangeTypeNodeInfo(changeType.getFile().getName(), changeType.getFile(), List.of(changeType))), rootNode, 0));
      }
    }
    rootNode.getInfo().setMembers(pChangeList);
    return treeUpdates;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
