package de.adito.git.impl.data;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Class to (temporarily) store information about an ssh key
 *
 * @author m.kaspera, 20.05.2019
 */
public class SSHKeyDetails
{

  private final String keyLocation;
  private final String remoteName;
  private final char[] passPhrase;

  public SSHKeyDetails(@Nullable String pKeyLocation, @NonNull String pRemoteName, @Nullable char[] pPassPhrase)
  {
    keyLocation = pKeyLocation;
    remoteName = pRemoteName;
    passPhrase = pPassPhrase;
  }

  /**
   * @return path to the ssh key, may be null
   */
  @Nullable
  public String getKeyLocation()
  {
    return keyLocation;
  }

  /**
   * @return the name of the remote this key should be assigned to
   */
  @NonNull
  public String getRemoteName()
  {
    return remoteName;
  }

  /**
   * @return passphrase for the key, call nullifyPassphrase as soon as you no longer need the passphrase in memory
   */
  @Nullable
  public char[] getPassPhrase()
  {
    return passPhrase;
  }

  /**
   * set all characters in the passPhrase char array to 0, thereby making the password unrecoverable and removing its representation from memory
   */
  public void nullifyPassphrase()
  {
    if (passPhrase != null)
    {
      Arrays.fill(passPhrase, '0');
    }
  }
}
