package de.adito.git.gui.dialogs.panels.TextPanes;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * JTextPane that does not wrap, which means it is actually useful in a ScrollPane
 * since the ScrollPane can actually do some work, instead of the JTextPane doing it needlessly.
 * Also useful for making sure the JTextPane doesn't get longer than intended because of line-wrapping
 *
 * @author m.kaspera, 13.12.2018
 */
class NonWrappingTextPane extends JTextPane
{
  NonWrappingTextPane()
  {
    super();
  }

  // Override getScrollableTracksViewportWidth
  // to preserve the full width of the text
  @Override
  public boolean getScrollableTracksViewportWidth()
  {
    Component parent = getParent();
    ComponentUI ui = getUI();

    return parent == null || (ui.getPreferredSize(this).width <= parent
        .getSize().width);
  }
}
