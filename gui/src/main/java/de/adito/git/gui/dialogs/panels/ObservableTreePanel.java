package de.adito.git.gui.dialogs.panels;

import de.adito.git.gui.tree.TreeUtil;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Base panel that allows showing a scrollPane or hiding it by showing a "Loading..." text instead
 *
 * @author m.kaspera, 19.08.2019
 */
public abstract class ObservableTreePanel extends JPanel
{
  protected final JLabel loadingLabel = new JLabel("Loading . . .");
  protected final JScrollPane treeScrollpane = new JScrollPane();
  protected final JPanel treeViewPanel = new JPanel(new BorderLayout());

  public ObservableTreePanel()
  {
    loadingLabel.setFont(new Font(loadingLabel.getFont().getFontName(), loadingLabel.getFont().getStyle(), 16));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
  }

  /**
   * remove the loading label and show the tree
   */
  protected void showTree()
  {
    TreeUtil.expandTreeInterruptible(getTree());
    treeViewPanel.remove(loadingLabel);
    treeViewPanel.add(treeScrollpane, BorderLayout.CENTER);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }

  /**
   * show the loading label and hide the tree
   */
  protected void showLoading()
  {
    treeViewPanel.remove(treeScrollpane);
    treeViewPanel.add(loadingLabel, BorderLayout.CENTER);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }

  protected abstract JTree getTree();
}
