package de.adito.git.gui.quickSearch;

import javax.swing.*;
import java.awt.BorderLayout;

/**
 * Panel that serves as view which has a ScrollPane for a QuickSearchable Component.
 *
 * @author m.kaspera, 08.02.2019
 */
public class SearchableView extends JPanel
{

  private final JScrollPane scrollPane = new JScrollPane();

  public SearchableView()
  {
    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
  }

  public void setSearchableComponent(JComponent pSearchableComponent)
  {
    scrollPane.setViewportView(pSearchableComponent);
  }

}
