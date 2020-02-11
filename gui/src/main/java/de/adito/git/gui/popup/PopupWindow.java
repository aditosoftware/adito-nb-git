package de.adito.git.gui.popup;

import javax.swing.*;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

/**
 * a implementation of the HeavyWightWindow to show all branches
 *
 * @author a.arnold, 12.11.2018
 */

public class PopupWindow extends JWindow
{
  private _WindowDisposer windowDisposer;

  public PopupWindow(Window parent, String pTitle, JComponent pComponent)
  {
    super(parent);
    windowDisposer = new _WindowDisposer();
    add(new PopupPanel(pComponent, pTitle, this));
    setType(Type.POPUP);
    try
    {
      setAlwaysOnTop(true);
    }
    catch (SecurityException se)
    {
      throw new RuntimeException();
    }
  }

  @Override
  public void setVisible(boolean b)
  {
    pack();
    super.setVisible(b);
    SwingUtilities.invokeLater(() -> Toolkit.getDefaultToolkit().addAWTEventListener(windowDisposer, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.KEY_EVENT_MASK));
  }

  private class _WindowDisposer implements AWTEventListener
  {

    @Override
    public void eventDispatched(AWTEvent event)
    {
      if (event.getID() != WindowEvent.WINDOW_OPENED)
      {
        if (event.getSource() != PopupWindow.this || !((WindowEvent) event).getWindow().getType().equals(Type.POPUP))
        {
          Toolkit.getDefaultToolkit().removeAWTEventListener(this);
          dispose();
        }
      }
    }
  }
}

