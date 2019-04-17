package de.adito.git.impl.data;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.data.IConfig;
import de.adito.git.impl.RepositoryImplHelper;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Implementation of IConfig, return values are the values in the config to the time the function is called,
 * always returns the config values of the repository with which it was created
 *
 * @author m.kaspera, 24.12.2018
 */
public class ConfigImpl implements IConfig
{

  private static final String USER_SECTION_KEY = "user";
  private static final String USER_NAME_KEY = "name";
  private static final String USER_EMAIL_KEY = "email";
  private static final String SSH_SECTION_KEY = "remote";
  private static final String SSH_KEY_KEY = "puttykeyfile";
  private static final String AUTO_CRLF_SECTION_KEY = "core";
  private static final String AUTO_CRLF_KEY = "autocrlf";
  private final IKeyStore keyStore;
  private final Git git;

  @Inject
  public ConfigImpl(IKeyStore pKeyStore, @Assisted Git pGit)
  {
    keyStore = pKeyStore;
    git = pGit;
  }

  @Override
  public @Nullable String getUserName()
  {
    return git.getRepository().getConfig().getString(USER_SECTION_KEY, null, USER_NAME_KEY);
  }

  @Override
  public @Nullable String getUserEmail()
  {
    return git.getRepository().getConfig().getString(USER_SECTION_KEY, null, USER_EMAIL_KEY);
  }

  @Override
  public @Nullable String getSshKeyLocation()
  {
    try
    {
      String remoteName = getRemoteName();
      if (remoteName == null)
        return null;
      return git.getRepository().getConfig().getString(SSH_SECTION_KEY, remoteName, SSH_KEY_KEY);
    }
    catch (IOException pE)
    {
      return null;
    }
  }

  @Nullable
  @Override
  public char[] getPassphrase()
  {
    String sshKeyLocation = getSshKeyLocation();
    return sshKeyLocation == null ? null : keyStore.read(sshKeyLocation);
  }

  @Nullable
  @Override
  public char[] getPassword()
  {
    try
    {
      String userName = getUserName();
      String remoteName = getRemoteName();
      return userName != null && remoteName != null ? keyStore.read(userName + remoteName) : null;
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public AUTO_CRLF getAutoCRLF()
  {
    String autoCRLFSetting = git.getRepository().getConfig().getString(AUTO_CRLF_SECTION_KEY, null, AUTO_CRLF_KEY);
    if ("true".equals(autoCRLFSetting))
      return AUTO_CRLF.TRUE;
    else if ("false".equals(autoCRLFSetting))
      return AUTO_CRLF.FALSE;
    else return AUTO_CRLF.INPUT;
  }

  @Override
  public void setUserName(@NotNull String pUserName)
  {
    git.getRepository().getConfig().setString(USER_SECTION_KEY, null, USER_NAME_KEY, pUserName);
    try
    {
      git.getRepository().getConfig().save();
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setUserEmail(@NotNull String pUserEmail)
  {
    git.getRepository().getConfig().setString(USER_SECTION_KEY, null, USER_EMAIL_KEY, pUserEmail);
    try
    {
      git.getRepository().getConfig().save();
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setSshKeyLocation(@Nullable String pSshKeyLocation)
  {
    try
    {
      String remoteName = getRemoteName();
      if (remoteName != null)
      {
        git.getRepository().getConfig().setString(SSH_SECTION_KEY, remoteName, SSH_KEY_KEY, pSshKeyLocation);
        git.getRepository().getConfig().save();
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setPassphrase(@Nullable char[] pPassphrase)
  {
    String sshKeyLocation = getSshKeyLocation();
    if (sshKeyLocation != null)
    {
      if (pPassphrase == null)
        keyStore.delete(sshKeyLocation);
      else
        keyStore.save(sshKeyLocation, pPassphrase, null);
    }
    else
    {
      throw new RuntimeException("Could not find any valid key in the config for which to set passphrase");
    }
  }

  @Override
  public void setPassword(@Nullable char[] pPassword)
  {
    try
    {
      String userName = getUserName();
      String remoteName = getRemoteName();
      String key = userName != null && remoteName != null ? userName + remoteName : null;
      if (key != null)
      {
        if (pPassword == null)
          keyStore.delete(key);
        else
          keyStore.save(key, pPassword, null);
      }
      else
      {
        throw new RuntimeException("Could not find any valid key in the config for which to set passphrase");
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * Figures out the name of the remote by using the tracked branch of the currently active branch, or the remote of master if the current branch does not have a
   * tracked branch
   *
   * @return name of the remote
   * @throws IOException if an exception occurs while JGit is reading the git config file
   */
  @Nullable
  private String getRemoteName() throws IOException
  {
    String remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(git, null);
    // Fallback: get remoteBranch of master and resolve remoteName with that branch
    if (remoteTrackingBranch == null)
      remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(git, "master");
    if (remoteTrackingBranch != null)
    {
      return git.getRepository().getRemoteName(remoteTrackingBranch);
    }
    return null;
  }

}
