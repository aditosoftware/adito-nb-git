package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.tree.models.*;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

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
  private final ObservableTreeUpdater<IFileChangeType> observableTreeUpdater;
  private final File projectDirectory;
  private final String callerName;

  @Inject
  SwitchTreeViewAction(IFileSystemUtil pFileSystemUtil, IPrefStore pPrefStore, @Assisted JTree pTree, @Assisted File pProjectDirectory, @Assisted String pCallerName,
                       @Assisted ObservableTreeUpdater<IFileChangeType> pObservableTreeUpdater)
  {
    fileSystemUtil = pFileSystemUtil;
    prefStore = pPrefStore;
    tree = pTree;
    observableTreeUpdater = pObservableTreeUpdater;
    projectDirectory = pProjectDirectory;
    callerName = pCallerName;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    BaseObservingTreeModel<IFileChangeType> treeModel;
    if (Constants.TREE_VIEW_FLAT.equals(prefStore.get(callerName + Constants.TREE_VIEW_TYPE_KEY)))
    {
      treeModel = new StatusTreeModel(projectDirectory);
      tree.setCellRenderer(new FileChangeTypeTreeCellRenderer(fileSystemUtil, projectDirectory));
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_HIERARCHICAL);
    }
    else
    {
      treeModel = new FlatStatusTreeModel(projectDirectory);
      tree.setCellRenderer(new FileChangeTypeFlatTreeCellRenderer(fileSystemUtil, projectDirectory));
      prefStore.put(callerName + Constants.TREE_VIEW_TYPE_KEY, Constants.TREE_VIEW_FLAT);
    }
    tree.setModel(treeModel);
    observableTreeUpdater.swapModel(treeModel);
  }
}
