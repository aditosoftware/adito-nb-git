package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import javax.swing.*;

/**
 * JTextPane that does not wrap, which means it is actually useful in a ScrollPane
 * since the ScrollPane can actually do some work, instead of the JTextPane doing it needlessly.
 * Also useful for making sure the JTextPane doesn't get longer than intended because of line-wrapping
 *
 * @author m.kaspera, 13.12.2018
 */
class NonWrappingTextPane extends JEditorPane
{
  NonWrappingTextPane()
  {
    super();
    // this is the simpleValueName of NetBeans "TEXT_LINE_WRAP"
    // here the second value can be replaced with "words" or "chars"
    putClientProperty("text-line-wrap", "none");
  }
}
