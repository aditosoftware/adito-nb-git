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
      String remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(git, null);
      // Fallback: get remoteBranch of master and resolve remoteName with that branch
      if (remoteTrackingBranch == null)
        remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(git, "master");
      if (remoteTrackingBranch == null)
        return null;
      String remoteName = git.getRepository().getRemoteName(remoteTrackingBranch);
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
    throw new UnsupportedOperationException("not implemented yet");
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
      String remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(git, null);
      // Fallback: get remoteBranch of master and resolve remoteName with that branch
      if (remoteTrackingBranch == null)
        remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(git, "master");
      if (remoteTrackingBranch != null)
      {
        String remoteName = git.getRepository().getRemoteName(remoteTrackingBranch);
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
    throw new UnsupportedOperationException("not implemented yet");
  }

}
