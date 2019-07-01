package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;

/**
 * @author m.kaspera, 17.03.2019
 */
class CollapseTreeAction extends AbstractAction
{

  private final JTree tree;

  @Inject
  CollapseTreeAction(IIconLoader pIconLoader, @Assisted JTree pTree)
  {
    super("Collapse nodes");
    tree = pTree;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.COLLAPSE_TREE_ACTION_ICON));
    putValue(Action.SHORT_DESCRIPTION, "Collapse all nodes");
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    ((DefaultTreeModel) tree.getModel()).reload();
  }
}

