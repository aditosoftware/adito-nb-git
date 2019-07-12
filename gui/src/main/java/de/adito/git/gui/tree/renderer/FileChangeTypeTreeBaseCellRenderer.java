package de.adito.git.gui.tree.renderer;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.io.File;

/**
 * Renderer with special logic for FileChangeTypeNodes, the additional info the the right of the name of the node is left to the subclasses
 *
 * @author m.kaspera, 26.02.2019
 */
public abstract class FileChangeTypeTreeBaseCellRenderer extends DefaultTreeCellRenderer
{

  static final String FILE_SINGULAR = "file";
  static final String FILE_PLURAL = "files";
  private static final int PANEL_HGAP = 5;
  private final IFileSystemUtil fileSystemUtil;
  private final DefaultTreeCellRenderer defaultRenderer;
  final File projectDir;

  public FileChangeTypeTreeBaseCellRenderer(IFileSystemUtil pFileSystemUtil, File pProjectDir)
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
      _addAdditionalInfo(nodeInfo, node, pLeaf, panel);
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
   * @param pNodeInfo information stored in the node
   * @param pNode     Node to be drawn
   * @param pLeaf     if the node is a leaf
   * @param pPanel    panel that is used to draw the row for the node
   */
  abstract void _addAdditionalInfo(FileChangeTypeNodeInfo pNodeInfo, FileChangeTypeNode pNode, boolean pLeaf, JPanel pPanel);


}
