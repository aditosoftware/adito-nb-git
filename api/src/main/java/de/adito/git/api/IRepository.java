package de.adito.git.api;

import de.adito.git.api.data.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IRepository {

    /**
     * @param addList List of files to add to staging
     */
    void add(List<File> addList) throws Exception;

    /**
     * @param message String with the commit message entered by the user
     * @return the ID of the commit as String
     */
    String commit(@NotNull String message) throws Exception;

    /**
     * @param message String with the commit message entered by the user
     * @param fileList List of files that should be committed (and none else)
     * @return the ID of the commit as String
     */
    String commit(@NotNull String message, List<File> fileList) throws Exception;

    /**
     * Pushes the changes made to HEAD onto the selected branch/remote
     *
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean push() throws Exception;

    /**
     * @param targetId the id/url of the branch/remote to pull from
     * @return {@code true} if the pull was successful, {@code false otherwise}
     */
    boolean pull(@NotNull String targetId) throws Exception;

    /**
     * @param original  the older ICommit
     * @param compareTo the later ICommit
     * @return IFileDiff that contains the differences between the two files
     */
    @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) throws Exception;

    /**
     * performs a diff of the working copy of the provided files with their version as it is in HEAD
     *
     * @param fileToDiff List of Files that should be diff'd with HEAD. pass null if all changes should be displayed
     * @return List of IFileDiff describing the differences of the files in the working copy and HEAD
     */
    @NotNull List<IFileDiff> diff(@Nullable List<File> fileToDiff) throws Exception;

    /**
     *
     * @param identifier String identifying the specific version of the file
     * @return the contents of the requested file as String
     * @throws IOException if an error occurs during transport/reading of the file
     */
    String getFileContents(String identifier) throws IOException;

    /**
     *
     * @param commitId the ID of the commit for the version of the file
     * @param filename the name of the file to be retrieved
     * @return identifying String for the specific version of the file
     * @throws IOException if an error occurs during transport/reading of the file
     */
    String getFileVersion(String commitId, String filename) throws  IOException;

    /**
     * @param url  the url from which to pull from, as String
     * @param dest the location on the local disk
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean clone(@NotNull String url, @NotNull File dest) throws IOException;

    /**
     * @return List of IFileStatus that describe the different staging states of the local files
     */
    @NotNull Observable<IFileStatus> getStatus();

    /**
     * @param files List of files that should be added to the .gitignore
     */
    void ignore(@NotNull List<File> files) throws IOException;

    /**
     * Excludes the listed files, basically a local .gitignore
     *
     * @param files List of files that should be added to the .git/info/exclude
     */
    void exclude(@NotNull List<File> files) throws IOException;

    /**
     * @param files List of files which should be reverted to the state in HEAD
     */
    void revertWorkDir(@NotNull List<File> files) throws Exception;

    /**
     *
     * @param files List of files that should be reset. Can also be null, in which case all changes are reset
     */
    void reset(@NotNull List<File> files) throws Exception;

    /**
     * @param identifier ID for the branch/commit to reset to
     * @param resetType  resetType which type of reset should be conducted {@see EResetType}
     */
    void reset(@NotNull String identifier, EResetType resetType) throws Exception;

    /**
     * @param branchName String with the name of the branch
     * @param checkout   {@code true} if the branch should be automatically checked out after it was created
     */
    void createBranch(@NotNull String branchName, boolean checkout) throws Exception;

    /**
     * @param branchName the name of the branch to delete
     */
    void deleteBranch(@NotNull String branchName) throws Exception;

    /**
     * @param branchName String with identifier of the branch to checkout
     */
    void checkout(@NotNull String branchName) throws Exception;

    /**
     * @param branch branch to checkout
     */
    void checkout(@NotNull IBranch branch) throws  Exception;

    /**
     * @return List with IMergeDiffs for all conflicting files. Empty list if no conflicting files exists
     * or if the branch to be merged cannot be read from the conflicting files
     */
    List<IMergeDiff> getMergeConflicts() throws Exception;

    /**
     * @param sourceName String with identifier of source branch
     * @param targetName String with identifier of target branch
     * @return List of IMergeDiffs, the list is empty if no merge conflict happened, else the list of IMergeDiffs describe the merge conflicts
     */
    List<IMergeDiff> merge(@NotNull String sourceName, @NotNull String targetName) throws Exception;

    /**
     *
     * @param commitId Id of the commit for which the changed files should be retrieved
     * @return List<String> detailing the changed files in the commit
     * @throws Exception if JGit encountered an error condition
     */
    List<String> getCommitedFiles(String commitId) throws Exception;

    /**
     * @param identifier String with identifier of the commit
     * @return ICommit describing the commit
     */
    ICommit getCommit(@NotNull String identifier) throws Exception;

    /**
     * @param sourceBranch IBranch for which all commits should be retrieved
     * @return List with all ICommits in the sourceBranch
     */
    List<ICommit> getCommits(IBranch sourceBranch) throws Exception;

    /**
     * @param forFile File for which all commits should be retrieved
     * @return List with ICommits that contains all commits that affected the file
     */
    List<ICommit> getCommits(File forFile) throws Exception;

    /**
     * @return the directory of the actual repository
     */

    String getDirectory();

    /**
     *
     * @return List of all Commits in the repository
     * @throws Exception An Error occurred while calling JGit
     */
    List<ICommit> getAllCommits() throws Exception;

    /**
     * @param branchString String with the string of the targeted branch
     * @return the IBranch for the given identifier
     */
    IBranch getBranch(String branchString) throws Exception;

    String getCurrentBranch() throws Exception;

    /**
     * @return List of all IBranches in the repository
     */
    @NotNull
    Observable<List<IBranch>> getBranches() throws Exception;

}
