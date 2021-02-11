package de.adito.git.api.data;

import de.adito.git.api.exception.UnknownRemoteRepositoryException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for retrieving configuration information for a repository/remote
 *
 * @author m.kaspera, 24.12.2018
 */
public interface IConfig
{

  String USER_SECTION_KEY = "user";
  String USER_NAME_KEY = "name";
  String USER_EMAIL_KEY = "email";
  String REMOTE_SECTION_KEY = "remote";
  String FETCH_SUBSECTION_KEY = "fetch";
  String SSH_KEY_KEY = "puttykeyfile";
  String AUTO_CRLF_SECTION_KEY = "core";
  String AUTO_CRLF_KEY = "autocrlf";
  String REMOTE_URL_KEY = "url";

  /**
   * Represents the three different autoCRLF setting available in git
   */
  enum AUTO_CRLF
  {
    TRUE,
    FALSE,
    INPUT;

    @Override
    public String toString()
    {
      if (this == TRUE)
        return "true";
      else if (this == FALSE)
        return "false";
      else return "input";
    }
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
   * @param pRemoteUrl Url of the remote for which the ssh key is searched for. can be null
   * @return ssh key location set for this repository/current remote or null if none set
   */
  @Nullable String getSshKeyLocation(@Nullable String pRemoteUrl);

  /**
   * returns the passphrase registered with the sshKey of this config or null if no passphrase saved/the key is not set
   * IMPORTANT: Overwrite the returned char array as soon as possible
   *
   * @param pSSHKeyLocation location of the key for which to retrieve the passphrase, null if not known
   * @return passphrase registered with the sshKey
   */
  @Nullable char[] getPassphrase(@NotNull String pSSHKeyLocation);

  /**
   * returns the password registered to the current user/repository/remote combination if available, else null
   * IMPORTANT: Overwrite the returned char array as soon as possible
   *
   * @return password registered with the user/repository/remote
   */
  @Nullable char[] getPassword();

  /**
   * tries to retrieve the specified value from the config
   *
   * @param pSectionKey    name of the section, e.g. "core" or "branch"
   * @param pSubSectionKey name of the subsection, e.g. "master". Basically the second part of the sections that have two parts, like branch or remote. Null otherwise
   * @param pName          Name of the key, e.g. "fetch" or "filemode"
   * @return value of the key if that key exists and has a value, null otherwise
   */
  @Nullable String get(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NotNull String pName);

  /**
   * @return List with the information about the remotes stored in the config
   */
  @NotNull List<IRemote> getRemotes();

  /**
   * checks the autocrlf setting of the repository/global config
   *
   * @return the autoCRLF setting
   */
  AUTO_CRLF getAutoCRLF();

  void setAutoCRLF(AUTO_CRLF pAUTOCrlf);

  /**
   * @param pUserName new name for the user
   */
  void setUserName(@NotNull String pUserName);

  /**
   * @param pUserEmail new email for the user
   */
  void setUserEmail(@NotNull String pUserEmail);

  /**
   * save the location of the ssh key
   *
   * @param pSshKeyLocation location of the ssh key, null means the default key (user_home/.ssh/id_rsa)
   * @param pRemoteUrl      Url of the remote for which the ssh key should be saved, may be null (remote is determined via current branch and its tracked branch)
   */
  void setSshKeyLocationForUrl(@Nullable String pSshKeyLocation, @Nullable String pRemoteUrl);

  /**
   * save the location of the ssh key
   *
   * @param pSshKeyLocation location of the ssh key, null means the default key (user_home/.ssh/id_rsa)
   * @param pRemoteName     name of the remote for which the ssh key should be stored
   */
  void setSshKeyLocation(@Nullable String pSshKeyLocation, @NotNull String pRemoteName);

  /**
   * save the passphrase for the given remote URL in the keyring
   *
   * @param pPassphrase     new passphrase for the ssh key, null means no passphrase required
   * @param pSSHKeyLocation path to the ssh key
   */
  void setPassphrase(@Nullable char[] pPassphrase, @Nullable String pSSHKeyLocation);

  /**
   * save the password for a remote URL in the keyring
   *
   * @param pPassword  new password for the user, null means no password is required
   * @param pRemoteUrl url of the remote, or null if not known
   */
  void setPassword(@Nullable char[] pPassword, @Nullable String pRemoteUrl) throws UnknownRemoteRepositoryException;

  /**
   * tries to store the specified value in the config
   *
   * @param pSectionKey    name of the section, e.g. "core" or "branch"
   * @param pSubSectionKey name of the subsection, e.g. "master". Basically the second part of the sections that have two parts, like branch or remote. Null otherwise
   * @param pName          Name of the key, e.g. "fetch" or "filemode"
   * @param pValue         The value that the key should be set to
   * @return true if the set operation was successful, false otherwise
   */
  boolean setValue(@Nullable String pSectionKey, @Nullable String pSubSectionKey, @NotNull String pName, @NotNull String pValue);

  /**
   * get the name of either the remote with the passes url or the remote that is connected to the current branch (if any is)
   *
   * @param pRemoteUrl url of the remote, or null if not known
   * @return the name of the remote with the passed url, the name of the remote of the current branch if known, or null if the remote of the current branch is not known
   */
  @Nullable String getRemoteName(@Nullable String pRemoteUrl);

  /**
   * get the url stored in the config for the given remote
   *
   * @param pRemoteName name of the remote, e.g. "origin"
   * @return url stored in the config for the given remote, or null if no such remote exists/no url is stored
   */
  @Nullable String getRemoteUrl(@Nullable String pRemoteName);

  /**
   * estasblishes the tracking relationship between a local and a remote branch by writing the necessary information in the config
   * the local branch must not have a tracked branch yet
   *
   * @param pBranchname       name of the local branch
   * @param pRemoteBranchname name of the remote branch
   * @param pRemoteName       name of the remote that the remote branch is on
   */
  void establishTrackingRelationship(@NotNull String pBranchname, @NotNull String pRemoteBranchname, @NotNull String pRemoteName);

  /**
   * save the remote
   *
   * @param pRemote remote to be saved
   */
  void saveRemote(@NotNull IRemote pRemote);

  /**
   * add a new remote
   *
   * @param pRemoteName name that the new remote should be called
   * @param pRemoteUrl  url of the remote
   * @return true if the new remote could be written to the config file, false otherwise
   */
  boolean addRemote(@NotNull String pRemoteName, @NotNull String pRemoteUrl);

}
