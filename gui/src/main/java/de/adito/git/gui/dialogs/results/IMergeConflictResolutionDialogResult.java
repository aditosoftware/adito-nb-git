package de.adito.git.gui.dialogs.results;

import de.adito.git.gui.dialogs.*;

/**
 * @author m.kaspera, 22.11.2019
 */
public
interface IMergeConflictResolutionDialogResult<S, T> extends IDialogResult<S, T>
{
  boolean isAcceptChanges();

  IDialogDisplayer.EButtons getSelectedButton();
}
