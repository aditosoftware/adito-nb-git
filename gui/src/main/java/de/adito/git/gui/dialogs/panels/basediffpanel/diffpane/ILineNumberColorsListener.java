package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import java.util.List;

/**
 * @author m.kaspera, 22.01.2019
 */
public interface ILineNumberColorsListener
{

  void lineNumberColorsChanged(int pModelNumber, List<LineNumberColor> pNewValue);

}
