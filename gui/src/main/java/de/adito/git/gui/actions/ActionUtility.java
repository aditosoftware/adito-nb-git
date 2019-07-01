package de.adito.git.gui.actions;

import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;

/**
 * @author m.kaspera, 01.07.2019
 */
class ActionUtility
{

  private static final String AUTMATIC_STASH_INFO = "Existing changes to working tree detected: if you continue these changes will be automatically stashed, " +
      "however it is safer to manually commit these files first. Continue nonetheless?";
  private static final String AUTOMATIC_STASH_CB_TEXT = "Don't ask me again and automatically stash the changes from now on";

  /**
   * Checks if autostash is was set by the user, and if not prompts the user if it the changes should be stashed and the process continue/automatically stashed from
   * now on and continue or if the process should be aborted
   *
   * @return true if the files should be automatically stashed and the action can proceed, false if the user wants to cancel the action
   */
  static boolean isAbortAutostash(IPrefStore pPrefStore, IDialogProvider pDialogProvider)
  {
    if (!String.valueOf(true).equals(pPrefStore.get(Constants.AUTOMATICALLY_STASH_LOCAL_CHANES)))
    {
      DialogResult<?, Boolean> dialogResult = pDialogProvider.showCheckboxPrompt(AUTMATIC_STASH_INFO, AUTOMATIC_STASH_CB_TEXT);
      if (!dialogResult.isPressedOk())
        return true;
      else if (dialogResult.getInformation())
        pPrefStore.put(Constants.AUTOMATICALLY_STASH_LOCAL_CHANES, String.valueOf(true));
    }
    return false;
  }

}
