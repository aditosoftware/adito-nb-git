package de.adito.git.gui.dialogs.results;

import de.adito.git.gui.dialogs.IDialogResult;

/**
 * @author m.kaspera, 22.11.2019
 */
public
interface IStashedCommitSelectionDialogResult<S, T> extends IDialogResult<S, T>
{
  boolean doUnStash();
}
