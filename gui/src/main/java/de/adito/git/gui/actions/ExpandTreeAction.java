package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author m.kaspera, 17.03.2019
 */
public class ExpandTreeAction extends AbstractAction
{

  private final JTree tree;

  @Inject
  public ExpandTreeAction(IIconLoader pIconLoader, @Assisted JTree pTree)
  {
    super("Expand nodes");
    tree = pTree;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.EXPAND_TREE_ACTION_ICON));
    putValue(Action.SHORT_DESCRIPTION, "Expand all nodes");
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    for (int i = 0; i < tree.getRowCount(); i++)
    {
      tree.expandRow(i);
    }
  }
}
