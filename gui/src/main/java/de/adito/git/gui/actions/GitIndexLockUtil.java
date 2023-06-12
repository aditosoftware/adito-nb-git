package de.adito.git.gui.actions;

import de.adito.git.api.*;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.panels.NotificationPanel;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.impl.Util;
import lombok.NonNull;

import java.io.IOException;

/**
 * Utility class for checking and handeling the index.lock file.
 *
 * @author r.hartinger
 */
public class GitIndexLockUtil
{

  private GitIndexLockUtil()
  {
  }
 
  /**
   * Checks for an existing {@code .git/index.lock} file. If such a file exists, the user will be prompted with a dialog where they have the option to delete the index.lock file.
   * <p>
   * This method should be called before a git action.
   *
   * @param pRepository     the git repository. This has the methods to check and delete the index.lock file
   * @param pDialogProvider the dialog provider, which is needed to provide the user with the dialog to delete the index.lock file
   * @param pNotifyUtil     the notificationUtil. This is only needed, when there is an error while deleting the index.lock file, so the user is notified
   */
  public static void checkAndHandleLockedIndexFile(@NonNull IRepository pRepository, @NonNull IDialogProvider pDialogProvider, @NonNull INotifyUtil pNotifyUtil)
  {
    // check for index.lock file. If it exists, give the user the possibility to delete the index.lock file, before doing a git action
    if (pRepository.checkForLockedIndexFile())
    {
      IUserPromptDialogResult<NotificationPanel, Object> indexLockDialog = pDialogProvider.showYesNoDialog(Util.getResource(GitIndexLockUtil.class, "indexLockTitle"), Util.getResource(GitIndexLockUtil.class, "indexLockText"));

      if (indexLockDialog.isOkay())
      {
        try
        {
          pRepository.deleteLockedIndexFile();
        }
        catch (IOException pE)
        {
          pNotifyUtil.notify(pE, "Error deleting the index.lock file", true);
        }
      }
    }
  }

}
