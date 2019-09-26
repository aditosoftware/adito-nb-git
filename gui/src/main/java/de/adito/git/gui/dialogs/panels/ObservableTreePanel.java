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
  private final JPanel loadingLabelPanel = new JPanel();
  protected final JScrollPane treeScrollpane = new JScrollPane();
  protected final JLayeredPane treeViewPanel = new JLayeredPane();

  public ObservableTreePanel()
  {
    JLabel loadingLabel = new JLabel("Loading . . .");
    loadingLabel.setFont(new Font(loadingLabel.getFont().getFontName(), loadingLabel.getFont().getStyle(), 16));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadingLabelPanel.setLayout(new BorderLayout());
    loadingLabelPanel.add(loadingLabel);
    treeViewPanel.setLayout(new LayeredBorderLayout());
    treeViewPanel.add(treeScrollpane, BorderLayout.CENTER, 1);
    treeViewPanel.add(loadingLabelPanel, BorderLayout.CENTER, 0);
  }

  /**
   * remove the loading label and show the tree
   */
  protected void showTree()
  {
    TreeUtil.expandTreeInterruptible(getTree());
    loadingLabelPanel.setVisible(false);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }

  /**
   * show the loading label and hide the tree
   */
  protected void showLoading()
  {
    loadingLabelPanel.setVisible(true);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }

  protected abstract JTree getTree();
}
