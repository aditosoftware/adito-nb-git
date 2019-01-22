package de.adito.git.impl.data;

import de.adito.git.api.data.IConfig;
import org.jetbrains.annotations.*;

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
  public @Nullable String getSshKeyLocation()
  {
    return sshKeyLocation;
  }

  @Nullable
  @Override
  public char[] getPassphrase()
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
  public void setSshKeyLocation(@Nullable String pSshKeyLocation)
  {
    sshKeyLocation = pSshKeyLocation;
  }

  @Override
  public void setPassphrase(@Nullable char[] pPassphrase)
  {
    passphrase = pPassphrase;
  }

  @Override
  public void setPassword(@Nullable char[] pPassword)
  {
    //not implemented
  }
}
