package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener for changes in a LineNumberColorModel
 *
 * @author m.kaspera, 22.01.2019
 */
public interface LineNumberColorsListener
{

  /**
   * This method is called if the LineNumberColors that a model keeps track of have changed
   *
   * @param pNewValue new value of the list of LineNumberColors that the model keeps track of
   */
  void lineNumberColorsChanged(@NotNull List<LineNumberColor> pNewValue);

}
