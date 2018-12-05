package de.adito.git.api;

import de.adito.git.api.data.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IRepository
{

  /**
   * @param addList List of files to add to staging
   * @throws AditoGitException if an error occurs
   */
  void add(List<File> addList) throws AditoGitException;

  /**
   * @param message String with the commit message entered by the user
   * @return the ID of the commit as String
   * @throws AditoGitException if an error occurs
   */
  String commit(@NotNull String message) throws AditoGitException;

  /**
   * @param message  String with the commit message entered by the user
   * @param fileList List of files that should be committed (and none else)
   * @param isAmend  If this commit should be amended to the previous commit
   * @return the ID of the commit as String
   * @throws AditoGitException if an error occurs
   */
  String commit(@NotNull String message, List<File> fileList, boolean isAmend) throws AditoGitException;

  /**
   * Pushes the changes made to HEAD onto the selected branch/remote
   *
   * @return {@code true} if the operation was successful, {@code false} otherwise
   * @throws AditoGitException if an error occurs
   */
  boolean push() throws AditoGitException;

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
   * @param prune whether references to branches/tags that no longer exist on the remote should be deleted or not
   * @throws AditoGitException if an error occurs
   */
  void fetch(boolean prune) throws AditoGitException;

  List<IFileChangeChunk> diff(@NotNull String fileContents, File compareWith) throws IOException;

  /**
   * @param original  the older ICommit
   * @param compareTo the later ICommit
   * @return IFileDiff that contains the differences between the two files
   * @throws AditoGitException if an error occurs
   */
  @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) throws AditoGitException;

  /**
   * performs a diff of the working copy of the provided files with their version as it is in compareWith
   *
   * @param fileToDiff  List of Files that should be diff'd with HEAD. pass null if all changes should be displayed
   * @param compareWith ICommit giving the version that the files should be compared with. If null files are compared to the HEAD
   * @return List of IFileDiff describing the differences of the files in the working copy and compareWith (or HEAD if compareWith == null)
   * @throws AditoGitException if an error occurs
   */
  @NotNull List<IFileDiff> diff(@Nullable List<File> fileToDiff, @Nullable ICommit compareWith) throws AditoGitException;

  /**
   * @param identifier String identifying the specific version of the file
   * @return the contents of the requested file as String
   * @throws IOException if an error occurs during transport/reading of the file
   */
  String getFileContents(String identifier) throws IOException;

  /**
   * @param commitId the ID of the commit for the version of the file
   * @param filename the name of the file to be retrieved
   * @return identifying String for the specific version of the file
   * @throws IOException if an error occurs during transport/reading of the file
   */
  String getFileVersion(String commitId, String filename) throws IOException;

  /**
   * @param url  the url from which to pull from, as String
   * @param dest the location on the local disk
   * @return {@code true} if the operation was successful, {@code false} otherwise
   * @throws IOException if an error occurs during transport/reading of the file
   */
  boolean clone(@NotNull String url, @NotNull File dest) throws IOException;

  /**
   * @return List of IFileStatus that describe the different staging states of the local files
   */
  @NotNull Observable<IFileStatus> getStatus();

  /**
   * @param files List of files that should be added to the .gitignore
   * @throws IOException if an error occurs during transport/reading of the file
   */
  void ignore(@NotNull List<File> files) throws IOException;

  /**
   * Excludes the listed files, basically a local .gitignore
   *
   * @param files List of files that should be added to the .git/info/exclude
   * @throws IOException if an error occurs during transport/reading of the file
   */
  void exclude(@NotNull List<File> files) throws IOException;

  /**
   * @param files List of files which should be reverted to the state in HEAD
   * @throws AditoGitException if an error occurs
   */
  void revertWorkDir(@NotNull List<File> files) throws AditoGitException;

  /**
   * @param files List of files that should be reset. Can also be null, in which case all changes are reset
   * @throws AditoGitException if an error occurs
   */
  void reset(@NotNull List<File> files) throws AditoGitException;

  /**
   * @param identifier ID for the branch/commit to reset to
   * @param resetType  resetType which type of reset should be conducted
   * @throws AditoGitException if an error occurs
   */
  void reset(@NotNull String identifier, @NotNull EResetType resetType) throws AditoGitException;

  /**
   * @param branchName String with the name of the branch
   * @param checkout   {@code true} if the branch should be automatically checked out after it was created
   * @throws AditoGitException if an error occurs
   */
  void createBranch(@NotNull String branchName, boolean checkout) throws AditoGitException;

  /**
   * @param branchName the name of the branch to delete
   * @throws AditoGitException if an error occurs
   */
  void deleteBranch(@NotNull String branchName) throws AditoGitException;

  /**
   * @param branchName String with identifier of the branch to checkout
   * @throws AditoGitException if an error occurs
   */
  void checkout(@NotNull String branchName) throws AditoGitException;

  /**
   * @param branch branch to checkout
   * @throws AditoGitException if an error occurs
   */
  void checkout(@NotNull IBranch branch) throws AditoGitException;

  /**
   * @return List with IMergeDiffs for all conflicting files. Empty list if no conflicting files exists
   * or if the branch to be merged cannot be read from the conflicting files
   * @throws AditoGitException if an error occurs
   */
  List<IMergeDiff> getMergeConflicts() throws AditoGitException;

  /**
   * @param sourceBranch The source branch
   * @param targetBranch The target branch
   * @return List of IMergeDiffs, the list is empty if no merge conflict happened, else the list of IMergeDiffs describe the merge conflicts
   * @throws AditoGitException if an error occurs
   */
  List<IMergeDiff> merge(@NotNull IBranch sourceBranch, @NotNull IBranch targetBranch) throws AditoGitException;

  /**
   * @param commitId Id of the commit for which the changed files should be retrieved
   * @return {@code List<String>} detailing the changed files in the commit
   * @throws AditoGitException if JGit encountered an error condition
   * @throws AditoGitException if an error occurs
   */
  List<IFileChangeType> getCommittedFiles(String commitId) throws AditoGitException;

  /**
   * @param identifier String with identifier of the commit, or NULL for the latest commit
   * @return ICommit describing the commit
   * @throws AditoGitException if an error occurs
   */
  ICommit getCommit(@Nullable String identifier) throws AditoGitException;

  /**
   * @param sourceBranch IBranch for which all commits should be retrieved. Pass NULL for all commits
   * @return List with all ICommits in the sourceBranch
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(IBranch sourceBranch) throws AditoGitException;

  /**
   * @param sourceBranch IBranch for which commits should be retrieved. Pass NULL for all commits
   * @param numCommits   how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with all ICommits in the sourceBranch
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(IBranch sourceBranch, int numCommits) throws AditoGitException;

  /**
   * @param sourceBranch IBranch for which commits should be retrieved. Pass NULL for all commits
   * @param indexFrom    how many commits should be skipped. Pass -1 if the parameter should be ignored
   * @param numCommits   how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with all ICommits in the sourceBranch
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(IBranch sourceBranch, int indexFrom, int numCommits) throws AditoGitException;

  /**
   * @param forFile File for which all commits should be retrieved. Pass NULL for all commits
   * @return List with ICommits that contains all commits that affected the file
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(File forFile) throws AditoGitException;

  /**
   * @param forFile    File for which commits should be retrieved. Pass NULL for all commits
   * @param numCommits how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with ICommits that contains all commits that affected the file
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(File forFile, int numCommits) throws AditoGitException;

  /**
   * @param forFile    File for which commits should be retrieved. Pass NULL for all commits
   * @param indexFrom  how many commits should be skipped. Pass -1 if the parameter should be ignored
   * @param numCommits how many commits should be loaded. Pass -1 if the parameter should be ignored
   * @return List with ICommits that contains all commits that affected the file
   * @throws AditoGitException if an error occurs
   */
  List<ICommit> getCommits(File forFile, int indexFrom, int numCommits) throws AditoGitException;

  /**
   * @param commits    List of commits for which the CommitHistoryTreeList should be created
   * @param startCHTLI If the List of commits is an extension of a list, pass the last CommitHistoryTreeListItem of that list here
   * @return List of CommitHistoryTreeListItems
   */
  List<CommitHistoryTreeListItem> getCommitHistoryTreeList(@NotNull List<ICommit> commits, @Nullable CommitHistoryTreeListItem startCHTLI);

  /**
   * @return the directory of the actual repository
   */
  String getDirectory();

  /**
   * @return the directory of the top-most directory covered by the VCS
   */
  File getTopLevelDirectory();

  /**
   * @param branchString String with the string of the targeted branch
   * @return the IBranch for the given identifier
   * @throws AditoGitException if an error occurs
   */
  IBranch getBranch(String branchString) throws AditoGitException;

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
}
