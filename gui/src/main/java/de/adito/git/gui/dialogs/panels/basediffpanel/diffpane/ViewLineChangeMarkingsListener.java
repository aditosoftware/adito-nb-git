package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener for any changes in a ViewLineChangeMarkingsModel
 *
 * @author m.kaspera, 09.12.2022
 */
public interface ViewLineChangeMarkingsListener
{

  /**
   * this method is called if the LineNumberColors of the ViewLineChangeMarkingsModel changed
   *
   * @param pAdaptedLineNumberColorList new list of LineNumberColors with the updated values
   */
  void viewLineChangeMarkingChanged(@NotNull List<LineNumberColor> pAdaptedLineNumberColorList);

}
