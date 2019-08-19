package de.adito.git.gui.dialogs.panels;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Base panel that allows showing a scrollPane or hiding it by showing a "Loading..." text instead
 *
 * @author m.kaspera, 19.08.2019
 */
public class ObservableTreePanel extends JPanel
{
  private final JLabel loadingLabel = new JLabel("Loading . . .");
  protected final JScrollPane treeScrollpane = new JScrollPane();
  protected final JPanel treeViewPanel = new JPanel(new BorderLayout());

  public ObservableTreePanel()
  {
    loadingLabel.setFont(new Font(loadingLabel.getFont().getFontName(), loadingLabel.getFont().getStyle(), 16));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
  }

  protected void _showTree()
  {
    treeViewPanel.remove(loadingLabel);
    treeViewPanel.add(treeScrollpane, BorderLayout.CENTER);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }

  protected void _showLoading()
  {
    treeViewPanel.remove(treeScrollpane);
    treeViewPanel.add(loadingLabel, BorderLayout.CENTER);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }
}
