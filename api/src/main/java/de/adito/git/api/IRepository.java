package de.adito.git.api;

import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IRepository
{

  /**
   * @param pAddList List of files to add to staging
   * @throws AditoGitException if an error occurs
   */
  void add(List<File> pAddList) throws AditoGitException;

  /**
   * @param pMessage String with the commit message entered by the user
   * @return the ID of the commit as String
   * @throws AditoGitException if an error occurs
   */
  String commit(@NotNull String pMessage) throws AditoGitException;

  /**
   * @param pMessage  String with the commit message entered by the user
   * @param pFileList List of files that should be committed (and none else)
   * @param pIsAmend  If this commit should be amended to the previous commit
   * @return the ID of the commit as String
   * @throws AditoGitException if an error occurs
   */
  String commit(@NotNull String pMessage, List<File> pFileList, boolean pIsAmend) throws AditoGitException;

  /**
   * Pushes the changes made to HEAD onto the selected branch/remote
   *
   * @return {@code true} if the operation was successful, {@code false} otherwise
   * @throws AditoGitException if an error occurs
   */
  Map<String, EPushResult> push() throws AditoGitException;

  /**
   * Pulls the current contents of the tracked remote branch of the currently selected local branch
   * from origin via pull --rebase
   *
   * @param pDoAbort true if the current Rebase should be aborted
   * @return {@code true} if the pull was successful, {@code false otherwise}
   * @throws AditoGitException if an error occurs
   */
  IRebaseResult pull(boolean pDoAbort) throws AditoGitException;

  /**
   * Fetches the current state of the remote and stores it internally. Does not affect the working directory.
   * See also the "git fetch" command
   *
   * @throws AditoGitException if an error occurs
   */
  void fetch() throws AditoGitException;

  /**
   * Fetches the current state of the remote and stores it internally. Does not affect the working directory.
   * See also the "git fetch" command
   *
   * @param pPrune whether references to branches/tags that no longer exist on the remote should be deleted or not
   * @throws AditoGitException if an error occurs
   */
  void fetch(boolean pPrune) throws AditoGitException;

  List<IFileChangeChunk> diff(@NotNull String pFileContents, File pCompareWith) throws IOException;

  /**
   * @param pOriginal  the older ICommit
   * @param pCompareTo the later ICommit
   * @return IFileDiff that contains the differences between the two files
   * @throws AditoGitException if an error occurs
   */
  @NotNull List<IFileDiff> diff(@NotNull ICommit pOriginal, @NotNull ICommit pCompareTo) throws AditoGitException;

  /**
   * performs a diff of the working copy of the provided files with their version as it is in compareWith
   *
   * @param pFileToDiff  List of Files that should be diff'd with HEAD. pass null if all changes should be displayed
   * @param pCompareWith ICommit giving the version that the files should be compared with. If null files are compared to the HEAD
   * @return List of IFileDiff describing the differences of the files in the working copy and compareWith (or HEAD if compareWith == null)
   * @throws AditoGitException if an error occurs
   */
  @NotNull List<IFileDiff> diff(@Nullable List<File> pFileToDiff, @Nullable ICommit pCompareWith) throws AditoGitException;

  String getFileContents(String pIdentifier, File pFile) throws IOException;

  /**
   * @param pIdentifier String identifying the specific version of the file
   * @return the contents of the requested file as String
   * @throws IOException if an error occurs during transport/reading of the file
   */
  String getFileContents(String pIdentifier) throws IOException;
  
  /**
   * @param file the File to check the status
   * @return returns the {@link IFileChangeType} of the file
   */
  IFileChangeType getStatusOfSingleFile(@NotNull File file);

  /**
   * @param pCommitId the ID of the commit for the version of the file
   * @param pFilename the name of the file to be retrieved
   * @return identifying String for the specific version of the file
   * @throws IOException if an error occurs during transport/reading of the file
   */
  String getFileVersion(String pCommitId, String pFilename) throws IOException;

  /**
   * @param pUrl  the url from which to pull from, as String
   * @param pDest the location on the local disk
   * @return {@code true} if the operation was successful, {@code false} otherwise
   * @throws IOException if an error occurs during transport/reading of the file
   */
  boolean clone(@NotNull String pUrl, @NotNull File pDest) throws IOException;

  /**
   * @return List of IFileStatus that describe the different staging states of the local files
   */
  @NotNull Observable<Optional<IFileStatus>> getStatus();

  /**
   * @param pFiles List of files that should be added to the .gitignore
   * @throws IOException if an error occurs during transport/reading of the file
   */
  void ignore(@NotNull List<File> pFiles) throws IOException;

  /**
   * Excludes the listed files, basically a local .gitignore
   *
   * @param pFiles List of files that should be added to the .git/info/exclude
   * @throws IOException if an error occurs during transport/reading of the file
   */
  void exclude(@NotNull List<File> pFiles) throws IOException;

  /**
   * @param pFiles List of files which should be reverted to the state in HEAD
   * @throws AditoGitException if an error occurs
   */
  void revertWorkDir(@NotNull List<File> pFiles) throws AditoGitException;

  /**
   * @param pFiles List of files that should be reset. Can also be null, in which case all changes are reset
   * @throws AditoGitException if an error occurs
   */
  void reset(@NotNull List<File> pFiles) throws AditoGitException;

  /**
   * @param pIdentifier ID for the branch/commit to reset to
   * @param pResetType  resetType which type of reset should be conducted
   * @throws AditoGitException if an error occurs
   */
  void reset(@NotNull String pIdentifier, @NotNull EResetType pResetType) throws AditoGitException;

  /**
   * @param pBranchName String with the name of the branch
   * @param pCheckout   {@code true} if the branch should be automatically checked out after it was created
   * @throws AditoGitException if an error occurs
   */
  void createBranch(@NotNull String pBranchName, boolean pCheckout) throws AditoGitException;

  /**
   * @param pBranchName the name of the branch to delete
   * @throws AditoGitException if an error occurs
   */
  void deleteBranch(@NotNull String pBranchName) throws AditoGitException;

  /**
   * Checks out the commit with id pId
   *
   * @param pId String with identifier of the object/commit to checkout
   * @throws AditoGitException if an error occurs, such as a CheckoutConflict or the id cannot be resolved
   */
  void checkout(@NotNull String pId) throws AditoGitException;

  /**
   * Checks out the version of the files under pPaths as it was in the commit with pId
   *
   * @param pId    String with identifier of the object/commit to checkout
   * @param pPaths paths of the files to checkout
   * @throws AditoGitException if an error occurs, such as a CheckoutConflict or the id cannot be resolved
   */
  void checkoutFileVersion(@NotNull String pId, List<String> pPaths) throws AditoGitException;

  /**
   * @param pBranch branch to checkout
   * @throws AditoGitException if an error occurs
   */
  void checkout(@NotNull IBranch pBranch) throws AditoGitException;

  /**
   * @return List with IMergeDiffs for all conflicting files. Empty list if no conflicting files exists
   * or if the branch to be merged cannot be read from the conflicting files
   * @throws AditoGitException if an error occurs or if the conflict was caused by a stashed commit, but several commits are stashed
   */
  List<IMergeDiff> getConflicts() throws AditoGitException;

  /**
   *
   * @param pStashedCommitId sha-1 id of the stashed commit that caused the conflicts
   * @return List with IMergeDiffs for all conflicting files. Empty list if no conflicting files exists
   * @throws AditoGitException if an error occurs
   */
  List<IMergeDiff> getStashConflicts(String pStashedCommitId) throws AditoGitException;

  /**
   * @param pSourceBranch The source branch
   * @param pTargetBranch The target branch
   * @return List of IMergeDiffs, the list is empty if no merge conflict happened, else the list of IMergeDiffs describe the merge conflicts
   * @throws AditoGitException if an error occurs
   */
  List<IMergeDiff> merge(@NotNull IBranch pSourceBranch, @NotNull IBranch pTargetBranch) throws AditoGitException;

  /**
   * @param pCommitId Id of the commit for which the changed files should be retrieved
   * @return {@code List<String>} detailing the changed files in the commit
   * @throws AditoGitException if JGit encountered an error condition
   * @throws AditoGitException if an error occurs
   */
  List<IFileChangeType> getCommittedFiles(String pCommitId) throws AditoGitException;

  /**
   * @param pIdentifier String with identifier of the commit, or NULL for the latest commit
   * @return ICommit describing the commit
   * @throws AditoGitException if an error occurs
   */
  ICommit getCommit(@Nullable String pIdentifier) throws AditoGitException;

  /**
   * @param pSourceBranch IBranch for which all commits should be retrieved. Pass NULL for all commits
   * @return List with all ICommits in the sourceBranch
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(IBranch pSourceBranch) throws AditoGitException;

  /**
   * @param pSourceBranch IBranch for which commits should be retrieved. Pass NULL for all commits
   * @param pNumCommits   how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with all ICommits in the sourceBranch
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(IBranch pSourceBranch, int pNumCommits) throws AditoGitException;

  /**
   * @param pSourceBranch IBranch for which commits should be retrieved. Pass NULL for all commits
   * @param pIndexFrom    how many commits should be skipped. Pass -1 if the parameter should be ignored
   * @param pNumCommits   how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with all ICommits in the sourceBranch
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(IBranch pSourceBranch, int pIndexFrom, int pNumCommits) throws AditoGitException;

  /**
   * @param pForFile File for which all commits should be retrieved. Pass NULL for all commits
   * @return List with ICommits that contains all commits that affected the file
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(File pForFile) throws AditoGitException;

  /**
   * @param pForFile    File for which commits should be retrieved. Pass NULL for all commits
   * @param pNumCommits how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with ICommits that contains all commits that affected the file
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(File pForFile, int pNumCommits) throws AditoGitException;

  /**
   * @param pForFile    File for which commits should be retrieved. Pass NULL for all commits
   * @param pIndexFrom  how many commits should be skipped. Pass -1 if the parameter should be ignored
   * @param pNumCommits how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with ICommits that contains all commits that affected the file
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(File pForFile, int pIndexFrom, int pNumCommits) throws AditoGitException;

  /**
   * retrieves all local commits that do not yet exist on the remote-tracking branch of the current branch
   *
   * @return list of all commits that are not yet pushed to the remote-tracking branch
   * @throws AditoGitException if JGit encounters an error
   */
  List<ICommit> getUnPushedCommits() throws AditoGitException;

  /**
   * @param pCommits    List of commits for which the CommitHistoryTreeList should be created
   * @param pStartCHTLI If the List of commits is an extension of a list, pass the last CommitHistoryTreeListItem of that list here
   * @return List of CommitHistoryTreeListItems
   */
  List<CommitHistoryTreeListItem> getCommitHistoryTreeList(@NotNull List<ICommit> pCommits, @Nullable CommitHistoryTreeListItem pStartCHTLI);

  /**
   * @return the directory of the actual repository
   */
  String getDirectory();

  /**
   * @return the directory of the top-most directory covered by the VCS
   */
  File getTopLevelDirectory();

  /**
   * @param pBranchString String with the string of the targeted branch
   * @return the IBranch for the given identifier
   * @throws AditoGitException if an error occurs
   */
  Optional<IBranch> getBranch(String pBranchString) throws AditoGitException;

  /**
   * @return the current branch
   * @throws AditoGitException if an error occurs
   */
  Observable<Optional<IBranch>> getCurrentBranch() throws AditoGitException;

  /**
   * @return List of all IBranches in the repository
   * @throws AditoGitException if an error occurs
   */
  @NotNull
  Observable<Optional<List<IBranch>>> getBranches() throws AditoGitException;

  /**
   * retrieve all stashed commits
   *
   * @return List of stashed commits
   * @throws AditoGitException if JGit encounters any errors
   */
  List<ICommit> getStashedCommits() throws AditoGitException;

  /**
   * check if there is any stashed commit and return the id of the latest stashed commit if any exist
   *
   * @return String with the sha-1 id of latest stashed commit if at least one stashed commits exists, else null
   * @throws AditoGitException if an error occurs
   */
  @Nullable
  String peekStash() throws AditoGitException;

  /**
   * Stash the uncommitted changes in the current working directory
   *
   * @return sha-1 id that the stashed commit with the changes that existed in the working directory got
   * @throws AditoGitException if an error occurs
   */
  @Nullable
  String stashChanges() throws AditoGitException;

  /**
   * un-stashed the latest stashed commit
   *
   * @return list with IMergeDiffs if a conflict occurs during un-stashing, empty list if successful
   * @throws AditoGitException if an error occurs
   */
  List<IMergeDiff> unStashIfAvailable() throws AditoGitException;

  /**
   * un-stash the changes of the specified stashed commit and drop the commit if the changes
   * could be successfully un-stashed
   *
   * @param pStashCommitId sha-1 id for the commit to be un-stashed
   * @return list with IMergeDiffs if a conflict occurs during un-stashing, empty list if successful
   * @throws AditoGitException if an error occurs
   */
  List<IMergeDiff> unStashChanges(@NotNull String pStashCommitId) throws AditoGitException;

  /**
   * drop the specified stashed commit
   *
   * @param pStashCommitId sha-1 id for the commit to deleted
   * @throws AditoGitException if an error occurs
   */
  void dropStashedCommit(@NotNull String pStashCommitId) throws AditoGitException;
}
