package de.adito.git.impl.data;

import de.adito.git.api.IKeyStore;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.data.IRemote;
import de.adito.git.api.prefs.IPrefStore;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author a.arnold, 14.01.2019
 */
public class CloneConfig implements IConfig
{

  private final IKeyStore keyStore;
  private final IPrefStore prefStore;
  private char[] passphrase;

  public CloneConfig(@NonNull IKeyStore pKeyStore, @NonNull IPrefStore pPrefStore)
  {
    keyStore = pKeyStore;
    prefStore = pPrefStore;
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
    return prefStore.get(pRemoteUrl);
  }

  @Nullable
  @Override
  public char[] getPassphrase(@NonNull String pSshKeyLocation)
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
  public @Nullable String get(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NonNull String pName)
  {
    return null;
  }

  @Override
  public @NonNull List<IRemote> getRemotes()
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
  public void setUserName(@NonNull String pUserName)
  {
    //not implemented
  }

  @Override
  public void setUserEmail(@NonNull String pUserEmail)
  {
    //not implemented
  }

  @Override
  public void setSshKeyLocationForUrl(@Nullable String pSshKeyLocation, @Nullable String pRemoteUrl)
  {
    if (pRemoteUrl != null)
      prefStore.put(pRemoteUrl, pSshKeyLocation);
  }

  @Override
  public void setSshKeyLocation(@Nullable String pSshKeyLocation, @NonNull String pRemoteName)
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
  public boolean setValue(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NonNull String pName, @NonNull String pValue)
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
  public void establishTrackingRelationship(@NonNull String pBranchname, @NonNull String pRemoteBranchname, @NonNull String pRemoteName)
  {
    // no implemented
  }

  @Override
  public void saveRemote(@NonNull IRemote pRemote)
  {
    // not implemented
  }

  @Override
  public boolean addRemote(@NonNull String pRemoteName, @NonNull String pRemoteUrl)
  {
    // not implemented
    return false;
  }

  public boolean removeRemote(@NonNull IRemote pRemote)
  {
    // not implemented
    return false;
  }
}
