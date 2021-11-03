package de.adito.git.api;

import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.data.ITag;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IProgressHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A helper interface for the clone wizard to get
 * control over the git commands
 *
 * @author a.arnold, 09.01.2019
 */
public interface ICloneRepo
{
  /**
   * Get the branches of one repository which is not cloned or downloaded yet.
   *
   * @param pUrl     The URL to check the branches
   * @param pSshPath The path of the SSH file (optional)
   * @return Returns a list of IBranches of the repository
   * @throws AditoGitException if any exceptions from JGit occur during branch retrieval (such as wrong password or invalid ssh key)
   */
  @NotNull
  List<IBranch> getBranchesFromRemoteRepo(@NotNull String pUrl, @Nullable String pSshPath) throws AditoGitException;

  /**
   * Get the tags of one repository which is not cloned or downloaded yet.
   *
   * @param pUrl     The URL of the remote repositoryto check the branches
   * @param pSshPath The path of the SSH file (optional)
   * @return the list of tags available in the repository
   * @throws AditoGitException if any exceptions from JGit occur during branch retrieval (such as wrong password or invalid ssh key)
   */
  @NotNull
  List<ITag> getTagsFromRemoteRepo(@NotNull String pUrl, @Nullable String pSshPath) throws AditoGitException;

  /**
   * Clone the repository from a URL to the local path
   *
   * @param pProgressHandle The handler for the progress
   * @param pLocalPath      The local path to clone
   * @param pURL            The URL to clone
   * @param pBranchName     The branch to checkout
   * @param pRemote         The remote to checkout
   * @param pTag            The tag to checkout
   * @param pSshPath        The path of the private SSH file (optional)
   * @param pProjectName    the project name
   * @throws AditoGitException if any error occurs during the clone
   */
  void cloneProject(@Nullable IProgressHandle pProgressHandle, @NotNull String pLocalPath, @NotNull String pProjectName,
                    @NotNull String pURL, @Nullable String pBranchName, @Nullable String pTag, @Nullable String pRemote,
                    String pSshPath) throws AditoGitException;

  /**
   * @return Config for this unfinished repo
   */
  IConfig getConfig();
}
