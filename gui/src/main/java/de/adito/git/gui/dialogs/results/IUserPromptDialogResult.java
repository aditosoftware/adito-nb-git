package de.adito.git.gui.dialogs.results;

import de.adito.git.gui.dialogs.IDialogResult;

/**
 * @author m.kaspera, 22.11.2019
 */
public
interface IUserPromptDialogResult<SOURCE_TYPE, RESULT_TYPE> extends IDialogResult<SOURCE_TYPE, RESULT_TYPE>
{
  boolean isOkay();
}
