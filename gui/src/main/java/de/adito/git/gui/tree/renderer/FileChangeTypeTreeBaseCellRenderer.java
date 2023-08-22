package de.adito.git.gui.tree.renderer;

import com.google.common.cache.*;
import de.adito.git.api.*;
import de.adito.git.gui.icon.MissingIcon;
import de.adito.git.gui.tree.nodes.*;
import lombok.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

/**
 * Renderer with special logic for FileChangeTypeNodes, the additional info the the right of the name of the node is left to the subclasses
 *
 * @author m.kaspera, 26.02.2019
 */
public abstract class FileChangeTypeTreeBaseCellRenderer extends DefaultTreeCellRenderer implements IDiscardable
{

  static final String FILE_SINGULAR = "file";
  static final String FILE_PLURAL = "files";
  private static final int PANEL_HGAP = 5;
  private static final Logger LOGGER = Logger.getLogger(FileChangeTypeTreeBaseCellRenderer.class.getName());
  private final IFileSystemUtil fileSystemUtil;
  private final DefaultTreeCellRenderer defaultRenderer;
  private final LoadingCache<CacheKey, AsyncIconLabel> asyncIconCache = CacheBuilder.newBuilder()
      .maximumSize(500)
      .build(new CacheLoader<>()
      {
        @Override
        public AsyncIconLabel load(@NonNull CacheKey key)
        {
          return new AsyncIconLabel(() -> {
            Image icon;
            icon = fileSystemUtil.getIcon(key.file, key.isExpanded);
            return icon;
          });
        }
      });
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
      File iconFile = pLeaf ? nodeInfo.getNodeFile() : projectDir;

      // icon for the file/folder
      JLabel iconLabel;
      try
      {
        CacheKey cacheKey = new CacheKey(iconFile, pExpanded, pLeaf);
        iconLabel = asyncIconCache.get(cacheKey);
      }
      catch (ExecutionException pE)
      {
        LOGGER.log(Level.WARNING, pE, () -> "Failed to load icon asynchronously");
        iconLabel = new JLabel(MissingIcon.get16x16());
      }
      panel.add(iconLabel, BorderLayout.WEST);

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

  @Override
  public void discard()
  {
    asyncIconCache.invalidateAll();
  }

  /**
   * Key for the cache, combines infos necessary for the icon such as:
   * - which file is referenced
   * - is the node expanded
   * - is the node a leaf
   */
  @AllArgsConstructor
  @EqualsAndHashCode
  private static final class CacheKey
  {
    final File file;
    final boolean isExpanded;
    final boolean isLeaf;

  }
}
