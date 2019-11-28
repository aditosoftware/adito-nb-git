package de.adito.git.gui.dialogs.results;

import de.adito.git.gui.dialogs.IDialogResult;

/**
 * @author m.kaspera, 28.11.2019
 */
public interface IChangeTrackedBranchDialogResult<S, T> extends IDialogResult<S, T>
{

  boolean isCancel();

  boolean isChangeBranch();

  boolean isKeepTrackedBranch();

}
