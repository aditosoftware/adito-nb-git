package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.tree.models.StatusTreeModel;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

/**
 * @author m.kaspera, 15.04.2019
 */
class RevertFilesDialog extends AditoBaseDialog<Object>
{


  @Inject
  public RevertFilesDialog(IActionProvider pActionProvider, IFileSystemUtil pFileSystemUtil, @Assisted List<IFileChangeType> pFilesToRevert, @Assisted File pProjectDir)
  {
    if (pFilesToRevert.size() > 1)
    {
      setPreferredSize(new Dimension(600, 500));
      setLayout(new BorderLayout(0, 10));

      // Label at the top
      JLabel topLabel = new JLabel("Revert the listed files back to the state of HEAD?");
      topLabel.setHorizontalAlignment(SwingConstants.CENTER);
      add(topLabel, BorderLayout.NORTH);

      // Tree and scrollPane for tree
      JTree fileTree = new JTree(new StatusTreeModel(Observable.just(pFilesToRevert), pProjectDir));
      fileTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil, pProjectDir));
      JScrollPane scrollPane = new JScrollPane(fileTree);
      add(scrollPane, BorderLayout.CENTER);

      // Toolbar
      JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
      toolBar.setFloatable(false);
      toolBar.add(pActionProvider.getExpandTreeAction(fileTree));
      toolBar.add(pActionProvider.getCollapseTreeAction(fileTree));
      add(toolBar, BorderLayout.WEST);
    }
    else if (pFilesToRevert.size() == 1)
    {
      add(new JLabel("Revert file " + pFilesToRevert.get(0).getFile().getAbsolutePath() + " back to the state of HEAD?"));
    }
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
