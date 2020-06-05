package de.adito.git.gui.swing;

import javax.swing.*;

/**
 * Contains utility methods that either invoke Swing methods or have something to do with Swing
 *
 * @author m.kaspera, 04.06.2020
 */
public class SwingUtil
{

  /**
   * Executes the given Runnable right now if the current Thread is already the EDT, else call invokeLater with the Runnable as argument
   *
   * @param pRunnable Runnbale to execute in the EDT as soon as possible
   */
  public static void invokeASAP(Runnable pRunnable)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      pRunnable.run();
    }
    else
    {
      SwingUtilities.invokeLater(pRunnable);
    }
  }

}
