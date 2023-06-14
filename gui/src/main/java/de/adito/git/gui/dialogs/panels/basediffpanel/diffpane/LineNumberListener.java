package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.swing.LineNumber;
import lombok.NonNull;

/**
 * Listener for changes in a LineNumberModel
 *
 * @author m.kaspera, 09.12.2022
 */
public interface LineNumberListener
{

  /**
   * @param pTextChangeEvent DeltaTextChangeEvent that triggered the re-calculation of the lineNumber y coordinates
   * @param pLineNumbers     Array containing a LineNumber Object for each line present in the editor. Contains the line number and coordinates at which the line number
   *                         should be drawn
   */
  void lineNumbersChanged(@NonNull IDeltaTextChangeEvent pTextChangeEvent, @NonNull LineNumber[] pLineNumbers);

}
