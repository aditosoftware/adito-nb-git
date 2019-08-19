package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.tree.TreeUtil;
import de.adito.git.gui.tree.models.DiffTreeModel;
import de.adito.git.gui.tree.models.FlatDiffTreeModel;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * @author m.kaspera, 15.07.2019
 */
class SwitchDiffTreeViewAction extends AbstractAction
{

  private final IFileSystemUtil fileSystemUtil;
  private final IPrefStore prefStore;
  private final JTree tree;
  private final Observable<List<IDiffInfo>> changeList;
  private final File projectDirectory;
  private final String callerName;

  @Inject
  SwitchDiffTreeViewAction(IIconLoader pIIconLoader, IFileSystemUtil pFileSystemUtil, IPrefStore pPrefStore, @Assisted JTree pTree,
                           @Assisted Observable<List<IDiffInfo>> pChangeList, @Assisted File pProjectDirectory, @Assisted String pCallerName)
  {
    callerName = pCallerName;
    putValue(Action.SMALL_ICON, pIIconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT));
    fileSystemUtil = pFileSystemUtil;
    prefStore = pPrefStore;
    tree = pTree;
    changeList = pChangeList;
    projectDirectory = pProjectDirectory;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    Runnable expandTreeRunnable = () -> TreeUtil.expandTreeInterruptible(tree);
    if (tree.getModel() instanceof IDiscardable)
      ((IDiscardable) tree.getModel()).discard();
    if (Constants.TREE_VIEW_FLAT.equals(prefStore.get(callerName + Constants.TREE_VIEW_TYPE_KEY)))
    {
      tree.setModel(new DiffTreeModel(changeList, projectDirectory));
      tree.setCellRenderer(new FileChangeTypeTreeCellRenderer(fileSystemUtil, projectDirectory));
      ((DiffTreeModel) tree.getModel()).invokeAfterComputations(expandTreeRunnable);
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_HIERARCHICAL);
    }
    else
    {
      tree.setModel(new FlatDiffTreeModel(changeList, projectDirectory));
      tree.setCellRenderer(new FileChangeTypeFlatTreeCellRenderer(fileSystemUtil, projectDirectory));
      ((FlatDiffTreeModel) tree.getModel()).invokeAfterComputations(expandTreeRunnable);
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_FLAT);
    }
  }
}
