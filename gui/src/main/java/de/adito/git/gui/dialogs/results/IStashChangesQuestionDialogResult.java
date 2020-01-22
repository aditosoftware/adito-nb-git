package de.adito.git.gui.dialogs.results;

import de.adito.git.gui.dialogs.IDialogResult;

/**
 * @author m.kaspera, 25.11.2019
 */
public interface IStashChangesQuestionDialogResult<S, T> extends IDialogResult<S, T>
{

  boolean isDiscardChanges();

  boolean isStashChanges();

  boolean isAbort();
}
