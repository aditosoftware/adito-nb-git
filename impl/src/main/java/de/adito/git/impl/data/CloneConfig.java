package de.adito.git.impl.data;

import de.adito.git.api.IKeyStore;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.data.IRemote;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author a.arnold, 14.01.2019
 */
public class CloneConfig implements IConfig
{

  private final IKeyStore keyStore;
  private char[] passphrase;
  private String sshKeyLocation;

  public CloneConfig(IKeyStore pKeyStore)
  {
    keyStore = pKeyStore;
  }


  @Override
  public @Nullable String getUserName()
  {
    return null;
  }

  @Override
  public @Nullable String getUserEmail()
  {
    return null;
  }

  @Override
  public @Nullable String getSshKeyLocation(String pRemoteUrl)
  {
    return sshKeyLocation;
  }

  @Nullable
  @Override
  public char[] getPassphrase(@NotNull String pSshKeyLocation)
  {
    if (passphrase == null)
      passphrase = keyStore.read(pSshKeyLocation);
    return passphrase;
  }

  @Nullable
  @Override
  public char[] getPassword()
  {
    return new char[0];
  }

  @Override
  public @Nullable String get(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NotNull String pName)
  {
    return null;
  }

  @Override
  public @NotNull List<IRemote> getRemotes()
  {
    return List.of();
  }

  @Override
  public AUTO_CRLF getAutoCRLF()
  {
    return null;
  }

  @Override
  public void setAutoCRLF(AUTO_CRLF pAUTOCrlf)
  {
    // not implemented
  }

  @Override
  public void setUserName(@NotNull String pUserName)
  {
    //not implemented
  }

  @Override
  public void setUserEmail(@NotNull String pUserEmail)
  {
    //not implemented
  }

  @Override
  public void setSshKeyLocationForUrl(@Nullable String pSshKeyLocation, String pRemoteUrl)
  {
    sshKeyLocation = pSshKeyLocation;
  }

  @Override
  public void setSshKeyLocation(@Nullable String pSshKeyLocation, @NotNull String pRemoteName)
  {
    // not implemented
  }

  @Override
  public void setPassphrase(@Nullable char[] pPassphrase, @Nullable String pSshKeyLocation)
  {
    passphrase = pPassphrase;
    if (pSshKeyLocation != null)
    {
      if (pPassphrase != null)
        keyStore.save(pSshKeyLocation, pPassphrase, null);
      else
        keyStore.delete(pSshKeyLocation);
    }
  }

  @Override
  public void setPassword(@Nullable char[] pPassword, @Nullable String pRemoteUrl)
  {
    //not implemented
  }

  @Override
  public boolean setValue(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NotNull String pName, @NotNull String pValue)
  {
    return false;
  }

  @Override
  public @Nullable String getRemoteName(@Nullable String pRemoteUrl)
  {
    //not implemented
    return null;
  }

  @Override
  public @Nullable String getRemoteUrl(@Nullable String pRemoteName)
  {
    //not implemented
    return null;
  }

  @Override
  public void establishTrackingRelationship(@NotNull String pBranchname, @NotNull String pRemoteBranchname, @NotNull String pRemoteName)
  {
    // no implemented
  }

  @Override
  public void saveRemote(@NotNull IRemote pRemote)
  {
    // not implemented
  }

  @Override
  public boolean addRemote(@NotNull String pRemoteName, @NotNull String pRemoteUrl)
  {
    // not implemented
    return false;
  }

  public boolean removeRemote(@NotNull IRemote pRemote)
  {
    // not implemented
    return false;
  }
}
