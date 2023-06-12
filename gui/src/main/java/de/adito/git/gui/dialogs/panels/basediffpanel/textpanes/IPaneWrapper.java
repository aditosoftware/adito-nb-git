package de.adito.git.gui.dialogs.panels.basediffpanel.textpanes;

import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.DiffPaneContainer;
import lombok.NonNull;

import javax.swing.*;

/**
 * Wrapper around a pane that consists of a EditorPane inside a DiffPane inside a ScrollPane
 *
 * @author m.kaspera, 14.11.2019
 */
public interface IPaneWrapper
{

  /**
   * @return EditorPane that is wrapped by this interface
   */
  @NonNull
  JEditorPane getEditorPane();

  /**
   * @return scrollPane that contains the editorPane that this wrapper wraps
   */
  @NonNull
  JScrollPane getScrollPane();

  /**
   * @return DiffPaneContainer that contains the editorPane and the lineNumber and ChoiceButtonPanels that are used to add information to the editorPane
   */
  @NonNull
  DiffPaneContainer getPaneContainer();

}
