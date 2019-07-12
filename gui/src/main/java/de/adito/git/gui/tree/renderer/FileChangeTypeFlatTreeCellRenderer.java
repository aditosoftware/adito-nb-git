package de.adito.git.gui.tree.renderer;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;

/**
 * Adds additional info to the right of the node name in the form of the path from the root folder (included) to the file of the FileChangeTypeNode if the node is a leaf
 * Otherwise the additional info is the number of leaves of the tree with node as root
 *
 * @author m.kaspera, 12.07.2019
 */
public class FileChangeTypeFlatTreeCellRenderer extends FileChangeTypeTreeBaseCellRenderer
{

  public FileChangeTypeFlatTreeCellRenderer(IFileSystemUtil pFileSystemUtil, File pProjectDir)
  {
    super(pFileSystemUtil, pProjectDir);
  }

  @Override
  void _addAdditionalInfo(FileChangeTypeNodeInfo pNodeInfo, FileChangeTypeNode pNode, boolean pLeaf, JPanel pPanel)
  {
    if (pLeaf && pNode.getLevel() != 0)
    {
      String labelText = projectDir.toPath().getParent().relativize(pNodeInfo.getNodeFile().toPath()).getParent().toString().replace("\\", " \\ ");
      if (pNodeInfo.getMembers().get(0).getChangeType() == EChangeType.RENAME)
      {
        labelText = labelText + " | moved from " + pNodeInfo.getMembers().get(0).getFile(EChangeSide.NEW).toPath()
            .relativize(pNodeInfo.getMembers().get(0).getFile(EChangeSide.OLD).toPath());
      }
      JLabel additionalInfoLabel = new JLabel(labelText);
      additionalInfoLabel.setEnabled(false);
      pPanel.add(additionalInfoLabel, BorderLayout.EAST);
    }
    else if (!pLeaf)
    {
      JLabel numChangedFilesLabel = new JLabel(pNodeInfo.getMembers().size()
                                                   + String.format(" %s changed", pNodeInfo.getMembers().size() == 1 ? FILE_SINGULAR : FILE_PLURAL));
      numChangedFilesLabel.setEnabled(false);
      pPanel.add(numChangedFilesLabel, BorderLayout.EAST);
    }
  }
}
