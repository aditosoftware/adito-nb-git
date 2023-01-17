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

  void lineNumberColorsChanged(@NotNull List<LineNumberColor> pNewValue);

}
