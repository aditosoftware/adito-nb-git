package de.adito.git.gui.swing;

import de.adito.git.api.data.IBranch;

import javax.swing.*;
import java.awt.Component;

/**
 * ListCellRenderer for IBranches, only the simple name is renderered instead of the full name. This is because the full name is seldom interesting for the end-user
 *
 * @author m.kaspera, 06.11.2019
 */
public class SimpleBranchNameListCellRenderer implements ListCellRenderer<IBranch>
{
  private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

  @Override
  public Component getListCellRendererComponent(JList<? extends IBranch> list, IBranch value, int index, boolean isSelected, boolean cellHasFocus)
  {
    return defaultListCellRenderer.getListCellRendererComponent(list, value.getSimpleName(), index, isSelected, cellHasFocus);
  }
}
