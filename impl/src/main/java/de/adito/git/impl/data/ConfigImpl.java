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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of IConfig, return values are the values in the config to the time the function is called,
 * always returns the config values of the repository with which it was created
 *
 * @author m.kaspera, 24.12.2018
 */
public class ConfigImpl implements IConfig
{

  private final IKeyStore keyStore;
  private final Logger logger = Logger.getLogger(ConfigImpl.class.getName());
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
  public @Nullable String getSshKeyLocation(String pRemoteUrl)
  {
    try
    {
      String remoteName = RepositoryImplHelper.getRemoteName(git, pRemoteUrl);
      if (remoteName == null)
        return null;
      String keyLocation = git.getRepository().getConfig().getString(SSH_SECTION_KEY, remoteName, SSH_KEY_KEY);
      logger.log(Level.WARNING, () -> String.format("git: key location for remote \"%s\" is \"%s\"", remoteName, keyLocation));
      return keyLocation;
    }
    catch (IOException pE)
    {
      return null;
    }
  }

  @Nullable
  @Override
  public char[] getPassphrase(@Nullable String pRemoteUrl)
  {
    String sshKeyLocation = getSshKeyLocation(pRemoteUrl);
    return sshKeyLocation == null ? null : keyStore.read(sshKeyLocation);
  }

  @Nullable
  @Override
  public char[] getPassword()
  {
    try
    {
      String userName = getUserName();
      String remoteName = RepositoryImplHelper.getRemoteName(git, null);
      logger.log(Level.WARNING, () -> String.format("git: retrieving password for user \"%s\" and realm \"%s\"", userName, remoteName));
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
  public void setSshKeyLocation(@Nullable String pSshKeyLocation, @Nullable String pRemoteUrl)
  {
    try
    {
      String remoteName = RepositoryImplHelper.getRemoteName(git, pRemoteUrl);
      if (remoteName != null)
      {
        logger.log(Level.WARNING, () -> String.format("git: Setting ssh key location for remote \"%s\" to %s", remoteName, pSshKeyLocation));
        git.getRepository().getConfig().setString(SSH_SECTION_KEY, remoteName, SSH_KEY_KEY, pSshKeyLocation);
        git.getRepository().getConfig().save();
      }
      else
      {
        logger.log(Level.WARNING, () -> String.format("git: Could not find remote for url \"%s\", ssh key location was not saved", pRemoteUrl));
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setPassphrase(@Nullable char[] pPassphrase, @Nullable String pRemoteUrl)
  {
    String sshKeyLocation = getSshKeyLocation(pRemoteUrl);
    if (sshKeyLocation != null)
    {
      if (pPassphrase == null)
      {
        keyStore.delete(sshKeyLocation);
        logger.log(Level.WARNING, () -> String.format("git: removed password for key %s", sshKeyLocation));
      }
      else
      {
        keyStore.save(sshKeyLocation, pPassphrase, null);
        logger.log(Level.WARNING, () -> String.format("git: saved password for key %s", sshKeyLocation));
      }
    }
    else
    {
      throw new RuntimeException("Could not find any valid key in the config for which to set passphrase. Passed url for the remote was: " + pRemoteUrl);
    }
  }

  @Override
  public void setPassword(@Nullable char[] pPassword, @Nullable String pRemoteUrl)
  {
    try
    {
      String userName = getUserName();
      String remoteName = RepositoryImplHelper.getRemoteName(git, pRemoteUrl);
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
        throw new RuntimeException("Could not find any valid key in the config for which to set password. Passed url for the remote was: " + pRemoteUrl);
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public @Nullable String getRemoteName(@Nullable String pRemoteUrl)
  {
    try
    {
      return RepositoryImplHelper.getRemoteName(git, pRemoteUrl);
    }
    catch (IOException pE)
    {
      return null;
    }
  }

  @Override
  public @Nullable String getRemoteUrl(@Nullable String pRemoteName)
  {
    return git.getRepository().getConfig().getString(SSH_SECTION_KEY, pRemoteName, REMOTE_URL_KEY);
  }

}
