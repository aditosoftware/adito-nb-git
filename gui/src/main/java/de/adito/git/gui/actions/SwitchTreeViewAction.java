package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.tree.TreeUtil;
import de.adito.git.gui.tree.models.FlatStatusTreeModel;
import de.adito.git.gui.tree.models.StatusTreeModel;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * Changes the model and cell renderer from flat to hierarchical(Model = FlatStatusTreeModel to a StatusTreeModel) and vice versa.
 *
 * @author m.kaspera, 12.07.2019
 */
class SwitchTreeViewAction extends AbstractAction
{

  private final IFileSystemUtil fileSystemUtil;
  private final IPrefStore prefStore;
  private final JTree tree;
  private final Observable<List<IFileChangeType>> changeList;
  private final File projectDirectory;
  private final String callerName;

  @Inject
  SwitchTreeViewAction(IFileSystemUtil pFileSystemUtil, IPrefStore pPrefStore, @Assisted JTree pTree,
                       @Assisted Observable<List<IFileChangeType>> pChangeList, @Assisted File pProjectDirectory, @Assisted String pCallerName)
  {
    fileSystemUtil = pFileSystemUtil;
    prefStore = pPrefStore;
    tree = pTree;
    changeList = pChangeList;
    projectDirectory = pProjectDirectory;
    callerName = pCallerName;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    Runnable expandTreeRunnable = () -> TreeUtil._expandTreeInterruptible(tree);
    if (tree.getModel() instanceof IDiscardable)
      ((IDiscardable) tree.getModel()).discard();
    if (Constants.TREE_VIEW_FLAT.equals(prefStore.get(callerName + Constants.TREE_VIEW_TYPE_KEY)))
    {
      tree.setModel(new StatusTreeModel(changeList, projectDirectory));
      tree.setCellRenderer(new FileChangeTypeTreeCellRenderer(fileSystemUtil, projectDirectory));
      ((StatusTreeModel) tree.getModel()).invokeAfterComputations(expandTreeRunnable);
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_HIERARCHICAL);
    }
    else
    {
      tree.setModel(new FlatStatusTreeModel(changeList, projectDirectory));
      tree.setCellRenderer(new FileChangeTypeFlatTreeCellRenderer(fileSystemUtil, projectDirectory));
      ((FlatStatusTreeModel) tree.getModel()).invokeAfterComputations(expandTreeRunnable);
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_FLAT);
    }
  }
}
