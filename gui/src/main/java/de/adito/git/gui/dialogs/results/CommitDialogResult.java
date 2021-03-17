package de.adito.git.gui.dialogs.results;

import java.io.File;
import java.util.List;

/**
 * Stores information about the user actions before pressing the commit/OK button in the
 * commit dialog, such as which files were selected to commit and if the commit should be amended
 *
 * @author m.kaspera 20.09.2018
 */
public class CommitDialogResult
{

  private final List<File> selectedFiles;
  private final boolean doAmend;
  private final String userName;
  private final String userMail;

  public CommitDialogResult(List<File> pSelectedFiles, boolean pDoAmend, String pUserName, String pUserMail)
  {
    this.selectedFiles = pSelectedFiles;
    this.doAmend = pDoAmend;
    userName = pUserName;
    userMail = pUserMail;
  }

  public List<File> getSelectedFiles()
  {
    return selectedFiles;
  }

  public boolean isDoAmend()
  {
    return doAmend;
  }

  public String getUserName()
  {
    return userName;
  }

  public String getUserMail()
  {
    return userMail;
  }
}
