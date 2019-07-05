package de.adito.git.impl.data;

import de.adito.git.api.data.IConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author a.arnold, 14.01.2019
 */
public class CloneConfig implements IConfig
{
  private char[] passphrase;
  private String sshKeyLocation;

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
  public char[] getPassphrase(String pRemoteUrl)
  {
    return passphrase;
  }

  @Nullable
  @Override
  public char[] getPassword()
  {
    return new char[0];
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
  public void setSshKeyLocation(@Nullable String pSshKeyLocation, String pRemoteUrl)
  {
    sshKeyLocation = pSshKeyLocation;
  }

  @Override
  public void setPassphrase(@Nullable char[] pPassphrase, @Nullable String pRemoteUrl)
  {
    passphrase = pPassphrase;
  }

  @Override
  public void setPassword(@Nullable char[] pPassword, @Nullable String pRemoteUrl)
  {
    //not implemented
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
}
