package de.adito.git.gui.quicksearch;

import com.jidesoft.swing.CheckBoxTree;
import de.adito.swing.KeyForwardAdapter;

import javax.swing.*;
import javax.swing.tree.TreeModel;

/**
 * @author m.kaspera, 19.02.2019
 */
public class SearchableCheckboxTree extends CheckBoxTree
{

  private KeyForwardAdapter keyForwardAdapter = null;

  /**
   * sets the model and initiales the keyAdapter bridge from this Tree to the specified panel
   *
   * @param pView  Panel that serves as the view for the tree and should get all key events from the tree
   * @param pModel TreeModel to set for this tree
   */
  public void init(JPanel pView, TreeModel pModel)
  {
    setModel(pModel);
    if (keyForwardAdapter != null)
      removeKeyListener(keyForwardAdapter);
    keyForwardAdapter = new KeyForwardAdapter(pView);
    addKeyListener(keyForwardAdapter);
  }
}
