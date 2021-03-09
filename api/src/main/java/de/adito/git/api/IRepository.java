package de.adito.git.api;

import de.adito.git.api.dag.IDAGFilterIterator;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface defining the actions that the Repository can perform
 *
 * @author m.kaspera 20.09.2018
 */
public interface IRepository extends IDiscardable
{

  String VOID_PATH = "/dev/null";

  /**
   * Maps all the different status that an repository can have
   */
  enum State
  {
    /**
     * an unfinished apply took place, skip or abort to resume normal work
     */
    APPLY,
    /**
     * bare repository, no work can take place
     */
    BARE,
    /**
     * bisecting state, normal work may continue but is discouraged
     */
    BISECTING,
    /**
     * normal repository state, all work can be done
     */
    SAFE,
    /**
     * merging took place and there are still conflicts to resolve
     */
    MERGING,
    /**
     * merging took place, conflicts are resolved but merge is not wrapped up yet
     */
    MERGING_RESOLVED,
    /**
     * cherry picking is taking place and there are still conflicts to resolve
     */
    CHERRY_PICKING,
    /**
     * cherry picking is taking place, all conflicts resolved but not wrapped up yet
     */
    CHERRY_PICKING_RESOLVED,
    /**
     * state of an unfinished rebase, resolve/skip/abort to continue normal work
     */
    REBASING,
    /**
     * unfinished rebase with merge
     */
    REBASING_MERGE,
    /**
     * interactive rebase is taking place
     */
    REBASING_INTERACTIVE,
    /**
     * unfinished rebase
     */
    REBASING_REBASING,
    /**
     * unfinished revert
     */
    REVERTING,
    /**
     * revert where all conflicts were resolved
     */
    REVERTING_RESOLVED
  }

  /**
   * retrieves the current state of the repository
   *
   * @return Enum of the State
   */
  Observable<Optional<IRepositoryState>> getRepositoryState();

  /**
   * adds all given files to the git staging
   *
   * @param pAddList List of files to add to staging
   * @throws AditoGitException if an error occurs
   */
  void add(List<File> pAddList) throws AditoGitException;

  /**
   * Removes all passed files from staging
   *
   * @param pList List of files to remove from staging
   * @throws AditoGitException if any of the files cannot be removed from staging
   */
  void remove(List<File> pList) throws AditoGitException;

  /**
   * Make sure all files have the lineEndings set by the gitattributes in the index
   *
   * @throws AditoGitException If any error occurrs while reading or writing the index
   */
  void renormalizeNewlines() throws AditoGitException;

  /**
   * performs a commit of the staged files, with the passed message as commit message. Message should not be empty
   *
   * @param pMessage String with the commit message entered by the user
   * @return the ID of the commit as String
   * @throws AditoGitException if an error occurs
   */
  String commit(@NotNull String pMessage) throws AditoGitException;

  /**
   * performs a commit of all staged files as well as the passed files.
   *
   * @param pMessage    String with the commit message entered by the user
   * @param pFileList   List of files that should be committed (and none else)
   * @param pAuthorName name of the author, null if default should be used
   * @param pAuthorMail email of the author, null if default should be used
   * @param pIsAmend    If this commit should be amended to the previous commit
   * @return the ID of the commit as String
   * @throws AditoGitException if an error occurs
   */
  String commit(@NotNull String pMessage, @NotNull List<File> pFileList, @Nullable String pAuthorName, @Nullable String pAuthorMail, boolean pIsAmend)
      throws AditoGitException;

  /**
   * Pushes the changes made to HEAD onto the selected branch/remote
   *
   * @param pIsPushTags true if you want to push tags, false otherwise. Use false as default value
   * @param pRemoteName A specific remote can be passed here if it is potentially unclear which remote to push to. Null if a remote tracking branch is configured or
   *                    the remote is otherwise known
   * @return {@code true} if the operation was successful, {@code false} otherwise
   * @throws AditoGitException if an error occurs
   */
  Map<String, EPushResult> push(boolean pIsPushTags, @Nullable String pRemoteName) throws AditoGitException;

  /**
   * Checks if the branch contains all changes from the second branch
   *
   * @param pBranch    the current branch/branch to check
   * @param pCompareTo the method checks if the changes from this branch are contained in the first branch. For a pull this would be the upstream branch
   * @return true if the changes from the second branch are contained in the first, false otherwise
   */
  boolean isUpToDate(@NotNull IBranch pBranch, @NotNull IBranch pCompareTo) throws AditoGitException;

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
   * Cherry picks the passed commits and puts them on top of the current HEAD
   *
   * @param pCommitList List of commits that should be cherry picked on top of the current HEAD
   * @return ICherryPickResult that describes the outcome of the operation. If the result is CONFLICT, the conflicting files can be found in getConflicts
   * @throws AditoGitException if any error occurs during the cherry pick operation
   */
  ICherryPickResult cherryPick(List<ICommit> pCommitList) throws AditoGitException;

  /**
   * Fetches the current state of the remote and stores it internally. Does not affect the working directory.
   * See also the "git fetch" command
   * If there are several remotes, fetches from all remotes
   *
   * @return List of trackingRefUpdates that were undertaken by the fetch, and their results. Can be checked for failed updates
   * @throws AditoGitException if an error occurs
   */
  List<ITrackingRefUpdate> fetch() throws AditoGitException;

  /**
   * Fetches the current state of the remote and stores it internally. Does not affect the working directory.
   * See also the "git fetch" command
   *
   * @param pPrune whether references to branches/tags that no longer exist on the remote should be deleted or not
   * @return List of trackingRefUpdates that were undertaken by the fetch, and their results. Can be checked for failed updates
   * @throws AditoGitException if an error occurs
   */
  List<ITrackingRefUpdate> fetch(boolean pPrune) throws AditoGitException;

  /**
   * Applies the patch contained in the passed file to the current state of the repository
   *
   * @param pPatchFile File that contains the patch data
   */
  void applyPatch(@NotNull File pPatchFile);

  /**
   * performs a diff of the working copy of the provided files with their version as it is in compareWith and writes the differences to the provided outputStream, in the
   * form of a git patch
   *
   * @param pFileToDiff  List of Files that should be diff'd with HEAD. pass null if all changes should be displayed
   * @param pCompareWith ICommit giving the version that the files should be compared with. If null files are compared to the HEAD
   * @param pWriteTo     OutputStream that the patch will be written to
   */
  void createPatch(@Nullable List<File> pFileToDiff, @Nullable ICommit pCompareWith, @NotNull OutputStream pWriteTo);

  /**
   * compare two commits and write their differences to the provided outputStream as patch
   *
   * @param pOriginal  the older ICommit
   * @param pCompareTo the later ICommit
   * @param pWriteTo   OutputStream that the patch will be written to
   */
  void createPatch(@NotNull ICommit pOriginal, @Nullable ICommit pCompareTo, @NotNull OutputStream pWriteTo);

  /**
   * Diff two strings, one coming from a file
   *
   * @param pString String to be compared with pFile
   * @param pFile   File whose current contents on the disk should be compared to pString
   * @return List of IFileChangeChunks containing the changed lines between the two versions
   * @throws IOException if an error during file read occurs
   */
  IFileDiff diffOffline(@NotNull String pString, @NotNull File pFile) throws IOException;

  /**
   * get the changed lines between a string and the contents of a file
   *
   * @param pFileContents String (with i.e. the current, unsaved contents of a file) that should be compared to the contents of pCompareWith
   * @param pCompareWith  File whose HEAD version should be compared to pFileContents
   * @return List of IFileChangeChunks containing the changed lines between the two versions
   * @throws IOException if an error during file read occurs
   */
  List<IChangeDelta> diff(@NotNull String pFileContents, File pCompareWith) throws IOException;

  /**
   * compare two commits, returns a list of IFileDiffs. Each IFileDiff contains the changes that occurred to one file between the commits
   *
   * @param pOriginal  the older ICommit
   * @param pCompareTo the later ICommit
   * @return IFileDiff that contains the differences between the two files
   * @throws AditoGitException if an error occurs
   */
  @NotNull List<IFileDiff> diff(@NotNull ICommit pOriginal, @Nullable ICommit pCompareTo) throws AditoGitException;

  /**
   * performs a diff of the working copy of the provided files with their version as it is in compareWith
   *
   * @param pFileToDiff  List of Files that should be diff'd with HEAD. pass null if all changes should be displayed
   * @param pCompareWith ICommit giving the version that the files should be compared with. If null files are compared to the HEAD
   * @return List of IFileDiff describing the differences of the files in the working copy and compareWith (or HEAD if compareWith == null)
   * @throws AditoGitException if an error occurs
   */
  @NotNull List<IFileDiff> diff(@Nullable List<File> pFileToDiff, @Nullable ICommit pCompareWith) throws AditoGitException;

  /**
   * Retrieve the encoding and contents of the specified file/version combination
   *
   * @param pIdentifier String identifying the specific version of the file
   * @param pFile       the file whose contents should be retrieved
   * @return the contents of the requested file as IFileContentInfo, with the content as String and the used encoding
   * @throws IOException if an error occurs during transport/reading of the file
   */
  IFileContentInfo getFileContents(String pIdentifier, File pFile) throws IOException;

  /**
   * Retrieve the encoding and contents of the specified file/version combination
   *
   * @param pIdentifier String identifying the specific version of the file
   * @return the contents of the requested file as IFileContentInfo, with the content as String and the used encoding
   */
  IFileContentInfo getFileContents(String pIdentifier);

  /**
   * retrieve the status of a single file. Should be faster than asking for the status of the whole repo and filtering for a single file
   *
   * @param pFile the File to check the status
   * @return returns the {@link IFileChangeType} of the file
   */
  IFileChangeType getStatusOfSingleFile(@NotNull File pFile);

  /**
   * retrieves the id for the object representing the specified file at the time of the specified commit
   *
   * @param pCommitId the ID of the commit for the version of the file
   * @param pFilename the name of the file to be retrieved
   * @return identifying String for the specific version of the file
   * @throws IOException if an error occurs during transport/reading of the file
   */
  String getFileVersion(String pCommitId, String pFilename) throws IOException;

  /**
   * clones the repository located at the given url to the pDest directory
   *
   * @param pUrl  the url from which to pull from, as String
   * @param pDest the location on the local disk
   * @return {@code true} if the operation was successful, {@code false} otherwise
   * @throws IOException if an error occurs during transport/reading of the file
   */
  boolean clone(@NotNull String pUrl, @NotNull File pDest) throws IOException;

  /**
   * Represents the status of the observable
   *
   * @return List of IFileStatus that describe the different staging states of the local files
   */
  @NotNull Observable<Optional<IFileStatus>> getStatus();

  /**
   * Ignores a given file
   *
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
   * reverts all files passed in the list to the state they have in HEAD
   *
   * @param pFiles List of files which should be reverted to the state in HEAD
   * @throws AditoGitException if an error occurs
   */
  void revertWorkDir(@NotNull List<File> pFiles) throws AditoGitException;

  /**
   * Reverts a list of commits (meaning you apply an "anti-patch" that negates the changes done)
   *
   * @param pCommitsToRevert List of commits that should be reverted
   * @throws AditoGitException if an error occurs
   */
  void revertCommit(@NotNull List<ICommit> pCommitsToRevert) throws AditoGitException;

  /**
   * resets all given files (basically the opposite of "git add")
   *
   * @param pFiles List of files that should be reset. Can also be null, in which case all changes are reset
   * @throws AditoGitException if an error occurs
   */
  void reset(@NotNull List<File> pFiles) throws AditoGitException;

  /**
   * Resets HEAD/the current branch to the given ID. The exact nature of the reset depends on the passed EResetType, check that Enum for more information about the
   * available types
   *
   * @param pIdentifier ID for the branch/commit to reset to
   * @param pResetType  resetType which type of reset should be conducted
   * @throws AditoGitException if an error occurs
   */
  void reset(@NotNull String pIdentifier, @NotNull EResetType pResetType) throws AditoGitException;

  /**
   * creates a new branch
   *
   * @param pBranchName String with the name of the branch
   * @param pStartPoint ICommit that is the base/source of the branch to create. pass null if it should be HEAD
   * @param pCheckout   {@code true} if the branch should be automatically checked out after it was created
   * @throws AditoGitException if an error occurs
   */
  void createBranch(@NotNull String pBranchName, @Nullable ICommit pStartPoint, boolean pCheckout) throws AditoGitException;

  /**
   * deletes the branch with the passed name
   *
   * @param pBranchName         the simple name of the branch to delete
   * @param pDeleteRemoteBranch if the remote-tracking branch should also be deleted
   * @param pIsForceDelete      if the deletion of the branch should be forced
   * @throws AditoGitException if an error occurs
   */
  void deleteBranch(@NotNull String pBranchName, boolean pDeleteRemoteBranch, boolean pIsForceDelete) throws AditoGitException;


  /**
   * get the blame annotations for one file
   *
   * @param pFile the file to get the annotations
   * @return an IBlame object
   */
  @NotNull
  Optional<IBlame> getBlame(@NotNull File pFile);

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
   * check out a branch
   *
   * @param pBranch branch to checkout
   * @throws AditoGitException if an error occurs
   */
  void checkout(@NotNull IBranch pBranch) throws AditoGitException;

  /**
   * checks out a remote branch and creates a new local branch (named pLocalName) that is tracking the remote branch
   *
   * @param pBranch    remote Branch to checkout
   * @param pLocalName how the local branch that tracks the remote branch should be named
   * @throws AditoGitException if the remote branch may not be checked out
   */
  void checkoutRemote(@NotNull IBranch pBranch, @NotNull String pLocalName) throws AditoGitException;

  /**
   * @return IMergeDetails with the list of IMergeDatas for all conflicting files (empty list if no conflicting files exists or if the branch to be merged
   * cannot be read from the conflicting files) and details about the origins of the conflicting versions
   * @throws AditoGitException if an error occurs or if the conflict was caused by a stashed commit, but several commits are stashed
   */
  IMergeDetails getConflicts() throws AditoGitException;

  /**
   * retrieves a list with all conflicting changes caused by the passed stash commit
   *
   * @param pStashedCommitId sha-1 id of the stashed commit that caused the conflicts
   * @return IMergeDetails with the list of IMergeDatas for all conflicting files (empty list if no conflicting files exists) and details about the origins of the
   * conflicting versions
   * @throws AditoGitException if an error occurs
   */
  IMergeDetails getStashConflicts(String pStashedCommitId) throws AditoGitException;

  /**
   * merges two branches
   *
   * @param pSourceBranch The source branch
   * @param pTargetBranch The target branch
   * @return List of IMergeDatas, the list is empty if no merge conflict happened, else the list of IMergeDatas describe the merge conflicts
   * @throws AditoGitException if an error occurs
   */
  List<IMergeData> merge(@NotNull IBranch pSourceBranch, @NotNull IBranch pTargetBranch) throws AditoGitException;

  /**
   * retrieves all files that were committed in the passed commit. One IDiffInfo represents the change to one parent commit, if the commit is a merge commit
   * several parent commits can exists and thus several IDiffInfos are returned
   *
   * @param pCommitId Id of the commit for which the changed files should be retrieved
   * @return {@code List<IDiffInfo>} detailing the changed files in the commit
   * @throws AditoGitException if JGit encountered an error condition
   * @throws AditoGitException if an error occurs
   */
  @NotNull
  List<IDiffInfo> getCommittedFiles(String pCommitId) throws AditoGitException;

  /**
   * retrieves the commit with the specified sha-1 id
   *
   * @param pIdentifier String with identifier of the commit, or NULL for HEAD
   * @return ICommit describing the commit
   * @throws AditoGitException if an error occurs
   */
  ICommit getCommit(@Nullable String pIdentifier) throws AditoGitException;

  /**
   * creates an iterator that can iterate over the commit history. The iterator filters out commits based on the passed commitFilter
   *
   * @param pCommitFilter Filter responsible for filtering all commits. Can contain things such as branch, files, number of commits etc.
   * @return List with all ICommits matching the filter
   * @throws AditoGitException if an error occurs
   */
  @NotNull
  IDAGFilterIterator<ICommit> getCommits(@NotNull ICommitFilter pCommitFilter) throws AditoGitException;

  /**
   * retrieves all local commits that do not yet exist on the remote-tracking branch of the current branch
   *
   * @return list of all commits that are not yet pushed to the remote-tracking branch
   * @throws AditoGitException if JGit encounters an error
   */
  @NotNull
  List<ICommit> getUnPushedCommits() throws AditoGitException;

  /**
   * @return the directory of the actual repository
   */
  String getDirectory();

  /**
   * @return Repository-DisplayName
   */
  @NotNull
  Observable<String> displayName();

  /**
   * @return the directory of the top-most directory covered by the VCS
   */
  File getTopLevelDirectory();

  /**
   * triggers a manual update of the status
   */
  void refreshStatus();

  /**
   * retrieves the branch for the given name. If possible pass the full name of the branch, otherwise the name must and will be changed to /refs/heads/pBranchString
   * by the method to make sure that an actual Branch is returned, instead of a Tag or some other reference with the same short name. This obviously means that
   * if only the simple name for a branch is given, the local branch is returned
   *
   * @param pBranchString String with the string of the targeted branch
   * @return the IBranch for the given identifier
   * @throws AditoGitException if an error occurs
   */
  IBranch getBranch(String pBranchString) throws AditoGitException;

  /**
   * @return List of all IBranches in the repository
   */
  @NotNull
  Observable<Optional<List<IBranch>>> getBranches();

  /**
   * create a new tag
   *
   * @param pName     name that the tag should have
   * @param pCommitId ID (sha-1) of the commit that the tag should point to. Null if the tag should point to current HEAD
   */
  void createTag(String pName, String pCommitId);

  /**
   * Delete the tag with the passed name
   *
   * @param pTag tag to be deleted
   * @return List of successfully deleted tags
   */
  @NotNull
  List<String> deleteTag(ITag pTag);

  /**
   * fetch a list of all tags of this repository
   *
   * @return List containing all the tags in this repository
   */
  @NotNull
  Observable<List<ITag>> getTags();

  /**
   * retrieve all stashed commits
   *
   * @return List of stashed commits
   * @throws AditoGitException if JGit encounters any errors
   */
  @NotNull
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
   * @param pMessage          Message that should be used as commit message for the stash commit
   * @param pIncludeUnTracked whether to stash ignored files or not. Default is false
   * @return sha-1 id that the stashed commit with the changes that existed in the working directory got
   * @throws AditoGitException if an error occurs
   */
  @Nullable
  String stashChanges(@Nullable String pMessage, boolean pIncludeUnTracked) throws AditoGitException;

  /**
   * un-stashed the latest stashed commit
   *
   * @return list with IMergeDatas if a conflict occurs during un-stashing, empty list if successful
   * @throws AditoGitException if an error occurs
   */
  @NotNull
  List<IMergeData> unStashIfAvailable() throws AditoGitException;

  /**
   * un-stash the changes of the specified stashed commit and drop the commit if the changes
   * could be successfully un-stashed
   *
   * @param pStashCommitId sha-1 id for the commit to be un-stashed
   * @return list with IMergeDatas if a conflict occurs during un-stashing, empty list if successful
   * @throws AditoGitException if an error occurs
   */
  @NotNull
  List<IMergeData> unStashChanges(@NotNull String pStashCommitId) throws AditoGitException;

  /**
   * drop the specified stashed commit
   *
   * @param pStashCommitId sha-1 id for the commit to deleted. If null the latest stashed commit is dropped
   * @throws AditoGitException if an error occurs
   */
  void dropStashedCommit(@Nullable String pStashCommitId) throws AditoGitException;

  /**
   * Creates a new IConfig object that provides the latest information about the config
   * with each call.
   *
   * @return IConfig object
   */
  IConfig getConfig();

  /**
   * get the names of all remotes referenced in the git config
   *
   * @return List with names of the remotes (e.g. "origin")
   */
  @NotNull
  List<IRemote> getRemotes();

  /**
   * Sets the update flag that is determines wether the status and other data is updated when files change.
   * The updateFlag works with a counter, so if setUpdate(false) is called twice, it takes two calls of setUpdateFlag(true) until the updateFlag is actually
   * active again. This makes sure that several sources can disable updates, and the updates only start up after the last source enables updates
   *
   * @param pIsActive true if updates should be enabled, false otherwise
   */
  void setUpdateFlag(boolean pIsActive);
}
