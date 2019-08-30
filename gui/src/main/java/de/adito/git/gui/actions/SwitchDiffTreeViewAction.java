package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.tree.models.*;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author m.kaspera, 15.07.2019
 */
class SwitchDiffTreeViewAction extends AbstractAction
{

  private final IFileSystemUtil fileSystemUtil;
  private final IPrefStore prefStore;
  private final JTree tree;
  private final File projectDirectory;
  private final ObservableTreeUpdater<IDiffInfo> observableTreeUpdater;
  private final String callerName;

  @Inject
  SwitchDiffTreeViewAction(IIconLoader pIIconLoader, IFileSystemUtil pFileSystemUtil, IPrefStore pPrefStore, @Assisted JTree pTree,
                           @Assisted ObservableTreeUpdater<IDiffInfo> pObservableTreeUpdater, @Assisted File pProjectDirectory, @Assisted String pCallerName)
  {
    observableTreeUpdater = pObservableTreeUpdater;
    callerName = pCallerName;
    putValue(Action.SMALL_ICON, pIIconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT));
    fileSystemUtil = pFileSystemUtil;
    prefStore = pPrefStore;
    tree = pTree;
    projectDirectory = pProjectDirectory;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    BaseObservingTreeModel<IDiffInfo> treeModel;
    if (Constants.TREE_VIEW_FLAT.equals(prefStore.get(callerName + Constants.TREE_VIEW_TYPE_KEY)))
    {
      treeModel = new DiffTreeModel(projectDirectory);
      tree.setCellRenderer(new FileChangeTypeTreeCellRenderer(fileSystemUtil, projectDirectory));
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_HIERARCHICAL);
    }
    else
    {
      treeModel = new FlatDiffTreeModel(projectDirectory);
      tree.setCellRenderer(new FileChangeTypeFlatTreeCellRenderer(fileSystemUtil, projectDirectory));
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_FLAT);
    }
    tree.setModel(treeModel);
    observableTreeUpdater.swapModel(treeModel);
  }
}
