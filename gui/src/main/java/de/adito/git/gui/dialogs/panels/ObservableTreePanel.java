package de.adito.git.gui.dialogs.panels;

import de.adito.git.impl.Util;

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
  private final JPanel noLocalChangesLabelPanel = new JPanel();
  protected final JScrollPane treeScrollpane = new JScrollPane();
  protected final JLayeredPane treeViewPanel = new JLayeredPane();

  protected ObservableTreePanel()
  {
    JLabel loadingLabel = new JLabel("Loading . . .");
    loadingLabel.setFont(new Font(loadingLabel.getFont().getFontName(), loadingLabel.getFont().getStyle(), 16));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadingLabelPanel.setLayout(new BorderLayout());
    loadingLabelPanel.add(loadingLabel);

    JLabel noLocalChangesLabel = new JLabel(Util.getResource(ObservableTreePanel.class, "noLocalChanges"));
    noLocalChangesLabel.setFont(new Font(loadingLabel.getFont().getFontName(), loadingLabel.getFont().getStyle(), 16));
    noLocalChangesLabel.setHorizontalAlignment(SwingConstants.CENTER);
    noLocalChangesLabelPanel.setLayout(new BorderLayout());
    noLocalChangesLabelPanel.add(noLocalChangesLabel);
    noLocalChangesLabelPanel.setVisible(false);

    treeViewPanel.setLayout(new LayeredBorderLayout());
    treeViewPanel.add(noLocalChangesLabelPanel, BorderLayout.CENTER, 2);
    treeViewPanel.add(treeScrollpane, BorderLayout.CENTER, 1);
    treeViewPanel.add(loadingLabelPanel, BorderLayout.CENTER, 0);
  }

  /**
   * remove the loading label and show the tree
   */
  protected void showTree()
  {
    loadingLabelPanel.setVisible(false);
    treeViewPanel.revalidate();
    treeViewPanel.repaint();
  }

  /**
   * Sets the "No Local Changes" Panel visible depending on the parameter.
   * @param pHasNoLocalChanges if {@code true} the panel will be displayed, otherwise the panel will not be displayed
   */
  protected void setNoLocalChangesPanelVisible(boolean pHasNoLocalChanges)
  {
    noLocalChangesLabelPanel.setVisible(pHasNoLocalChanges);
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
