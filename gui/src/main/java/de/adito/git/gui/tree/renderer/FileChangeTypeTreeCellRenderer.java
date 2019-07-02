package de.adito.git.gui.tree.renderer;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.io.File;

/**
 * Renderer with special logic for FileChangeTypeNodes
 *
 * @author m.kaspera, 26.02.2019
 */
public class FileChangeTypeTreeCellRenderer extends DefaultTreeCellRenderer
{

  private static final String FILE_SINGULAR = "file";
  private static final String FILE_PLURAL = "files";
  private static final int PANEL_HGAP = 5;
  private final IFileSystemUtil fileSystemUtil;
  private final File projectDir;
  private final DefaultTreeCellRenderer defaultRenderer;

  public FileChangeTypeTreeCellRenderer(IFileSystemUtil pFileSystemUtil, File pProjectDir)
  {
    fileSystemUtil = pFileSystemUtil;
    projectDir = pProjectDir;
    defaultRenderer = new DefaultTreeCellRenderer();
  }

  @Override
  public Component getTreeCellRendererComponent(JTree pTree, Object pValue, boolean pSelected, boolean pExpanded, boolean pLeaf,
                                                int pRow, boolean pHasFocus)
  {
    if (pValue instanceof FileChangeTypeNode)
    {
      FileChangeTypeNode node = (FileChangeTypeNode) pValue;
      FileChangeTypeNodeInfo nodeInfo = node.getInfo();
      if (nodeInfo == null)
        return defaultRenderer.getTreeCellRendererComponent(pTree, pValue, pSelected, pExpanded, pLeaf, pRow, pHasFocus);
      JPanel panel = new JPanel(new BorderLayout(PANEL_HGAP, 0));
      // icon for the file/folder
      JLabel iconLabel = new JLabel();
      Image icon = fileSystemUtil.getIcon(nodeInfo.getNodeFile(), pExpanded);
      if (icon == null && !pLeaf)
      {
        icon = fileSystemUtil.getIcon(projectDir, pExpanded);
      }
      if (icon != null)
      {
        iconLabel.setIcon(new ImageIcon(icon));
        panel.add(iconLabel, BorderLayout.WEST);
      }
      // name of the file/folder, if nodes are collapsed the path from the parentNode to the childNode
      JLabel fileLabel = new JLabel(nodeInfo.getNodeDescription());
      if (!pSelected && pLeaf && !nodeInfo.getMembers().isEmpty())
      {
        fileLabel.setForeground(nodeInfo.getMembers().get(0).getChangeType().getStatusColor());
      }
      panel.add(fileLabel, BorderLayout.CENTER);
      // if the node is not a leaf, write how many leaves the tree with node as root has
      _addAdditionalInfo(pLeaf, nodeInfo, panel);
      if (pSelected)
      {
        panel.setBackground(getBackgroundSelectionColor());
      }
      return panel;
    }
    return defaultRenderer.getTreeCellRendererComponent(pTree, pValue, pSelected, pExpanded, pLeaf, pRow, pHasFocus);
  }

  /**
   * Add additional info to the right of the file label, info can be things such as number of leaves or rename description if the file was renamed
   *
   * @param pLeaf     if the node is a leaf
   * @param pNodeInfo information stored in the node
   * @param pPanel    panel that is used to draw the row for the node
   */
  private void _addAdditionalInfo(boolean pLeaf, FileChangeTypeNodeInfo pNodeInfo, JPanel pPanel)
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
