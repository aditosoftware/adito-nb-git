package de.adito.git.gui.tree.renderer;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;

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
  private final DefaultTreeCellRenderer defaultRenderer;

  public FileChangeTypeTreeCellRenderer(IFileSystemUtil pFileSystemUtil)
  {
    fileSystemUtil = pFileSystemUtil;
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
      JPanel panel = new JPanel(new BorderLayout(PANEL_HGAP, 0));
      // icon for the file/folder
      JLabel iconLabel = new JLabel();
      Image icon = fileSystemUtil.getIcon(nodeInfo.getNodeFile(), pExpanded);
      if (icon != null)
      {
        iconLabel.setIcon(new ImageIcon(icon));
        panel.add(iconLabel, BorderLayout.WEST);
      }
      // name of the file/folder, if nodes are collapsed the path from the parentNode to the childNode
      JLabel fileLabel = new JLabel(nodeInfo.getNodeDescription());
      if (!pSelected && node.isLeaf())
      {
        fileLabel.setForeground(nodeInfo.getMembers().get(0).getChangeType().getStatusColor());
      }
      panel.add(fileLabel, BorderLayout.CENTER);
      // if the node is not a leaf, write how many leaves the tree with node as root has
      if (!node.isLeaf())
      {
        JLabel numChangedFilesLabel = new JLabel(nodeInfo.getMembers().size()
                                                     + String.format(" %s changed", nodeInfo.getMembers().size() == 1 ? FILE_SINGULAR : FILE_PLURAL));
        numChangedFilesLabel.setEnabled(false);
        panel.add(numChangedFilesLabel, BorderLayout.EAST);
      }
      return panel;
    }
    return defaultRenderer.getTreeCellRendererComponent(pTree, pValue, pSelected, pExpanded, pLeaf, pRow, pHasFocus);
  }
}
