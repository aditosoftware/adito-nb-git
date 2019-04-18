package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.UserInfo;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IUserInputPrompt;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;

/**
 * Holds the information about the password/passphrase/ssh key location for the user
 * Also has the methods that get called by the Jsch session to prompt the user for input if any of these information are missing
 *
 * @author m.kaspera, 20.12.2018
 */
public class GitUserInfo implements UserInfo
{

  private final IUserInputPrompt userInputPrompt;
  private final IKeyStore keyStore;
  private String passPhrase;
  private String password;
  private File sshKeyFile;

  @Inject
  GitUserInfo(IUserInputPrompt pUserInputPrompt, IKeyStore pKeyStore,
              @Nullable @Assisted("passphrase") String pPassPhrase,
              @Nullable @Assisted("password") String pPassword)
  {
    keyStore = pKeyStore;
    passPhrase = pPassPhrase;
    password = pPassword;
    userInputPrompt = pUserInputPrompt;
    sshKeyFile = null;
  }

  void setPassPhrase(String pPassPhrase)
  {
    passPhrase = pPassPhrase;
  }

  void setPassword(String pPassword)
  {
    password = pPassword;
  }

  @Override
  public String getPassphrase()
  {
    return passPhrase;
  }

  @Override
  public String getPassword()
  {
    return password;
  }

  public File getSshKeyFile()
  {
    return sshKeyFile;
  }

  public void setSshKeyFile(File pSshKeyFile)
  {
    sshKeyFile = pSshKeyFile;
  }

  @Override
  public boolean promptPassword(String pMessage)
  {
    IUserInputPrompt.PromptResult result = userInputPrompt.promptPassword(pMessage);
    password = result.getUserInput();
    return result.isPressedOK();
  }

  @Override
  public boolean promptPassphrase(String pMessage)
  {
    if (sshKeyFile != null)
    {
      char[] savedPassphrase = keyStore.read(sshKeyFile.getAbsolutePath());
      if (savedPassphrase != null)
      {
        passPhrase = String.valueOf(savedPassphrase);
        // null out the char array with the password
        Arrays.fill(savedPassphrase, '0');
        return true;
      }

    }
    IUserInputPrompt.PromptResult result = userInputPrompt.promptSSHInfo(pMessage, sshKeyFile == null ? null : sshKeyFile.getAbsolutePath(), null, keyStore);
    passPhrase = String.valueOf(result.getUserArrayInput());
    if (sshKeyFile != null && !passPhrase.isEmpty())
    {
      keyStore.save(sshKeyFile.getAbsolutePath(), result.getUserArrayInput(), null);
    }
    return result.isPressedOK();
  }

  @Override
  public boolean promptYesNo(String pMessage)
  {
    return userInputPrompt.promptYesNo(pMessage).isPressedOK();
  }

  @Override
  public void showMessage(String pMessage)
  {
    throw new UnsupportedOperationException();
  }

}
