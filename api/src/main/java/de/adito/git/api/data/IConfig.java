package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for retrieving configuration information for a repository/remote
 *
 * @author m.kaspera, 24.12.2018
 */
public interface IConfig
{

  /**
   * Represents the three different autoCRLF setting available in git
   */
  enum AUTO_CRLF
  {
    TRUE,
    FALSE,
    INPUT
  }

  /**
   * returns the username set for this repository/current remote, or the username stored in the global git settings if no
   * username is set for this specific repository/current remote
   *
   * @return the username as string or null if none can be found
   */
  @Nullable String getUserName();

  /**
   * returns the user email set for this repository/current remote, or the user email stored in the global git settings if no
   * user email is set for this specific repository/current remote
   *
   * @return Email address as string or null if none can be found
   */
  @Nullable String getUserEmail();

  /**
   * returns the ssh key set for this repository/current remote, if none is set the default ssh key (id_rsa), or null
   * if the default ssh key doesn't exist either
   *
   * @return ssh key location set for this repository/current remote or null if none set
   * @param pRemoteUrl Url of the remote for which the ssh key is searched for. can be null
   */
  @Nullable String getSshKeyLocation(@Nullable String pRemoteUrl);

  /**
   * returns the passphrase registered with the sshKey of this config or null if no passphrase saved/the key is not set
   * IMPORTANT: Overwrite the returned char array as soon as possible
   *
   * @return passphrase registered with the sshKey
   */
  @Nullable char[] getPassphrase();

  /**
   * returns the password registered to the current user/repository/remote combination if available, else null
   * IMPORTANT: Overwrite the returned char array as soon as possible
   *
   * @return password registered with the user/repository/remote
   */
  @Nullable char[] getPassword();

  /**
   * checks the autocrlf setting of the repository/global config
   *
   * @return the autoCRLF setting
   */
  AUTO_CRLF getAutoCRLF();

  /**
   * @param pUserName new name for the user
   */
  void setUserName(@NotNull String pUserName);

  /**
   * @param pUserEmail new email for the user
   */
  void setUserEmail(@NotNull String pUserEmail);

  /**
   * @param pSshKeyLocation location of the ssh key, null means the default key (user_home/.ssh/id_rsa)
   * @param pRemoteUrl Url of the remote for which the ssh key should be saved, may be null (remote is determined via current branch and its tracked branch)
   */
  void setSshKeyLocation(@Nullable String pSshKeyLocation, @Nullable String pRemoteUrl);

  /**
   * @param pPassphrase new passphrase for the ssh key, null means no passphrase required
   */
  void setPassphrase(@Nullable char[] pPassphrase);

  /**
   * @param pPassword new password for the user, null means no password is required
   */
  void setPassword(@Nullable char[] pPassword);


}
