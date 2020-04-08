package de.adito.git.gui.tree.renderer;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;

/**
 * Only adds additional info to a leaf node if the file was renamed
 * Nodes that are not leaves have the number of leaves of the tree with the node as root written
 *
 * @author m.kaspera, 12.07.2019
 */
public class FileChangeTypeTreeCellRenderer extends FileChangeTypeTreeBaseCellRenderer
{

  public FileChangeTypeTreeCellRenderer(IFileSystemUtil pFileSystemUtil, File pProjectDir)
  {
    super(pFileSystemUtil, pProjectDir);
  }

  @Override
  void _addAdditionalInfo(FileChangeTypeNodeInfo pNodeInfo, FileChangeTypeNode pNode, boolean pLeaf, JPanel pPanel)
  {
    if (!pLeaf)
    {
      JLabel numChangedFilesLabel = new JLabel(pNodeInfo.getMembers().size()
                                                   + String.format(" %s changed", pNodeInfo.getMembers().size() == 1 ? FILE_SINGULAR : FILE_PLURAL));
      numChangedFilesLabel.setEnabled(false);
      pPanel.add(numChangedFilesLabel, BorderLayout.EAST);
    }
    else if (pNodeInfo.getMembers().size() == 1 && pNodeInfo.getMembers().get(0).getChangeType() == EChangeType.RENAME)
    {
      JLabel renamedLabel = new JLabel("moved from "
                                           + pNodeInfo.getMembers().get(0).getFile(EChangeSide.NEW).toPath()
          .relativize(pNodeInfo.getMembers().get(0).getFile(EChangeSide.OLD).toPath()));
      pPanel.add(renamedLabel, BorderLayout.EAST);
    }
  }
}
