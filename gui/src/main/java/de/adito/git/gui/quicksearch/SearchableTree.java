package de.adito.git.gui.quicksearch;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author m.kaspera, 19.02.2019
 */
public class SearchableTree extends JTree
{

  private _KeyForwardAdapter keyForwardAdapter = null;

  public void init(JPanel pView, TreeModel pModel)
  {
    setModel(pModel);
    if (keyForwardAdapter != null)
      removeKeyListener(keyForwardAdapter);
    keyForwardAdapter = new _KeyForwardAdapter(pView);
    addKeyListener(keyForwardAdapter);
  }

  /**
   * KeyAdapter that forwards all KeyEvents to the Component that the QuickSearch is attached to
   */
  private class _KeyForwardAdapter extends KeyAdapter
  {

    private JPanel receiver;

    _KeyForwardAdapter(JPanel pReceiver)
    {
      receiver = pReceiver;
    }

    @Override
    public void keyTyped(KeyEvent pEvent)
    {
      receiver.dispatchEvent(pEvent);
    }
  }
}
