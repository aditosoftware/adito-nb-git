package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.DiffPaneContainer;

import javax.swing.*;

/**
 * Wrapper around a pane that consists of a EditorPane inside a DiffPane inside a ScrollPane
 *
 * @author m.kaspera, 14.11.2019
 */
public interface IPaneWrapper
{

  JEditorPane getEditorPane();

  JScrollPane getScrollPane();

  DiffPaneContainer getPaneContainer();

}
