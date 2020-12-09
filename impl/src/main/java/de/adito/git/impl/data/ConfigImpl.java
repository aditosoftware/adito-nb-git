package de.adito.git.impl.data;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.data.IRemote;
import de.adito.git.api.exception.UnknownRemoteRepositoryException;
import de.adito.git.impl.RepositoryImplHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.UserConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

  private static final String CONFIG_WRITE_ERROR_MSG = "Error while saving the config";
  private final IKeyStore keyStore;
  private final Logger logger = Logger.getLogger(ConfigImpl.class.getName());
  private final INotifyUtil notifyUtil;
  private final Git git;

  @Inject
  public ConfigImpl(IKeyStore pKeyStore, INotifyUtil pNotifyUtil, @Assisted Git pGit)
  {
    keyStore = pKeyStore;
    notifyUtil = pNotifyUtil;
    git = pGit;
  }

  @Override
  public @Nullable String getUserName()
  {
    return git.getRepository().getConfig().get(UserConfig.KEY).getAuthorName();
  }

  @Override
  public @Nullable String getUserEmail()
  {
    return git.getRepository().getConfig().get(UserConfig.KEY).getAuthorEmail();
  }

  @Override
  public @Nullable String getSshKeyLocation(String pRemoteUrl)
  {
    try
    {
      String remoteName = RepositoryImplHelper.getRemoteName(git, pRemoteUrl);
      if (remoteName == null)
        return null;
      String keyLocation = git.getRepository().getConfig().getString(REMOTE_SECTION_KEY, remoteName, SSH_KEY_KEY);
      logger.log(Level.INFO, () -> String.format("git: key location for remote \"%s\" is \"%s\"", remoteName, keyLocation));
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
      logger.log(Level.INFO, () -> String.format("git: retrieving password for user \"%s\" and realm \"%s\"", userName, remoteName));
      return userName != null && remoteName != null ? keyStore.read(userName + remoteName) : null;
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public @Nullable String get(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NotNull String pName)
  {
    return git.getRepository().getConfig().getString(pSectionKey, pSubSectionKey, pName);
  }

  @Override
  public @NotNull List<IRemote> getRemotes()
  {
    List<IRemote> remotes = new ArrayList<>();
    Set<String> remoteNames = git.getRepository().getRemoteNames();
    StoredConfig config = git.getRepository().getConfig();
    for (String remoteName : remoteNames)
    {
      String url = config.getString(REMOTE_SECTION_KEY, remoteName, "url");
      String fetchInfo = config.getString(REMOTE_SECTION_KEY, remoteName, FETCH_SUBSECTION_KEY);
      remotes.add(new RemoteImpl(remoteName, url, fetchInfo));
    }
    return remotes;
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
  public void setAutoCRLF(AUTO_CRLF pAUTOCrlf)
  {
    logger.log(Level.INFO, () -> String.format("git: setting AUTO_CRLF setting to %s", pAUTOCrlf.toString()));
    StoredConfig config = git.getRepository().getConfig();
    config.setString(AUTO_CRLF_SECTION_KEY, null, AUTO_CRLF_KEY, pAUTOCrlf.toString());
    try
    {
      config.save();
    }
    catch (IOException pE)
    {
      notifyUtil.notify("Config", "Error while trying to save the config, see IDE log for further details", false);
      logger.log(Level.SEVERE, pE, () -> CONFIG_WRITE_ERROR_MSG);
    }
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
  public void setSshKeyLocationForUrl(@Nullable String pSshKeyLocation, @Nullable String pRemoteUrl)
  {
    try
    {
      String remoteName = RepositoryImplHelper.getRemoteName(git, pRemoteUrl);
      if (remoteName != null)
      {
        setSshKeyLocation(pSshKeyLocation, remoteName);
      }
      else
      {
        logger.log(Level.INFO, () -> String.format("git: Could not find remote for url \"%s\", ssh key location was not saved", pRemoteUrl));
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setSshKeyLocation(@Nullable String pSshKeyLocation, @NotNull String pRemoteName)
  {
    try
    {
      logger.log(Level.INFO, () -> String.format("git: Setting ssh key location for remote \"%s\" to %s", pRemoteName, pSshKeyLocation));
      git.getRepository().getConfig().setString(REMOTE_SECTION_KEY, pRemoteName, SSH_KEY_KEY, pSshKeyLocation);
      git.getRepository().getConfig().save();
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setPassphrase(@Nullable char[] pPassphrase, @Nullable String pRemoteUrl) throws UnknownRemoteRepositoryException
  {
    String sshKeyLocation = getSshKeyLocation(pRemoteUrl);
    if (sshKeyLocation != null)
    {
      if (pPassphrase == null)
      {
        keyStore.delete(sshKeyLocation);
        logger.log(Level.INFO, () -> String.format("git: removed password for key %s", sshKeyLocation));
      }
      else
      {
        keyStore.save(sshKeyLocation, pPassphrase, null);
        logger.log(Level.INFO, () -> String.format("git: saved password for key %s", sshKeyLocation));
      }
    }
    else
    {
      throw new UnknownRemoteRepositoryException("Could not find any valid key in the config for which to set passphrase. Passed url for the remote was: " + pRemoteUrl);
    }
  }

  @Override
  public void setPassword(@Nullable char[] pPassword, @Nullable String pRemoteUrl) throws UnknownRemoteRepositoryException
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
        throw new UnknownRemoteRepositoryException("Could not find any valid key in the config for which to set password. Passed url for the remote was: " + pRemoteUrl);
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public boolean setValue(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NotNull String pName, @NotNull String pValue)
  {
    StoredConfig config = git.getRepository().getConfig();
    config.setString(pSectionKey, pSubSectionKey, pName, pValue);
    try
    {
      config.save();
    }
    catch (IOException pE)
    {
      logger.log(Level.SEVERE, pE, () -> CONFIG_WRITE_ERROR_MSG);
      return false;
    }
    return true;
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
    return git.getRepository().getConfig().getString(REMOTE_SECTION_KEY, pRemoteName, REMOTE_URL_KEY);
  }

  @Override
  public void establishTrackingRelationship(@NotNull String pBranchname, @NotNull String pRemoteBranchname, @NotNull String pRemoteName)
  {
    logger.log(Level.INFO, () -> String.format("Git: establishing tracking relationsship between %s and remote branch %s on remote %s", pBranchname, pRemoteBranchname,
                                               pRemoteName));
    StoredConfig config = git.getRepository().getConfig();
    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, pBranchname, REMOTE_SECTION_KEY, pRemoteName);
    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, pBranchname, "merge", pRemoteBranchname);
    try
    {
      config.save();
    }
    catch (IOException pE)
    {
      logger.log(Level.SEVERE, pE, () -> CONFIG_WRITE_ERROR_MSG);
    }
  }

  @Override
  public void saveRemote(@NotNull IRemote pRemote)
  {
    StoredConfig config = git.getRepository().getConfig();
    config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, pRemote.getName(), "url", pRemote.getUrl());
    config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, pRemote.getName(), FETCH_SUBSECTION_KEY, pRemote.getFetchInfo());
    try
    {
      config.save();
    }
    catch (IOException ignored)
    {
      // ignore exception
    }
  }

  @Override
  public boolean addRemote(@NotNull String pRemoteName, @NotNull String pRemoteUrl)
  {
    StoredConfig config = git.getRepository().getConfig();
    config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, pRemoteName, "url", pRemoteUrl);
    config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, pRemoteName, FETCH_SUBSECTION_KEY, "+refs/heads/*:refs/remotes/" + pRemoteName + "/*");
    try
    {
      config.save();
    }
    catch (IOException pE)
    {
      return false;
    }
    return true;
  }

  @Override
  public boolean removeRemote(@NotNull IRemote pRemote)
  {
    StoredConfig config = git.getRepository().getConfig();
    config.unsetSection(REMOTE_SECTION_KEY, pRemote.getName());
    try
    {
      config.save();
    }
    catch (IOException pE)
    {
      return false;
    }
    return true;
  }

}
