package de.adito.git.gui.tree.renderer;

import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.Constants;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Renderer for the Tree with tags, icon differs between tags themselves and synthetic "folders" for tags (/ denominates step between folders)
 *
 * @author m.kaspera, 07.06.2019
 */
public class TagTreeCellRenderer extends DefaultTreeCellRenderer
{

  private final IIconLoader iconLoader;

  public TagTreeCellRenderer(IIconLoader pIIconLoader)
  {
    iconLoader = pIIconLoader;
  }

  @Override
  public Component getTreeCellRendererComponent(JTree pTree, Object pValue, boolean pSelected, boolean pExpanded, boolean pLeaf,
                                                int pRow, boolean pHasFocus)
  {
    JPanel panel = new JPanel(new BorderLayout());
    JLabel iconLabel;
    if (pLeaf)
    {
      iconLabel = new JLabel(iconLoader.getIcon(Constants.TAG_ICON));
    }
    else
    {
      iconLabel = new JLabel(iconLoader.getIcon(Constants.SHOW_TAGS_ACTION_ICON));
    }
    panel.add(iconLabel, BorderLayout.WEST);
    panel.add(new JLabel(pValue.toString()), BorderLayout.CENTER);
    return panel;
  }
}
