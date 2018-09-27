package de.adito.git.api;

import org.jetbrains.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IGit {

    /**
     * @param addList List of files to add to staging
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean add(List<File> addList);

    /**
     * @param message String with the commit message entered by the user
     * @return the ID of the commit as String
     */
    String commit(@NotNull String message);

    /**
     * Pushes the changes made to HEAD onto the selected branch/remote
     *
     * @param targetId the id/url of the branch/remote to push to
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean push(@NotNull String targetId);

    /**
     * @param targetId the id/url of the branch/remote to pull from
     * @return {@code true} if the pull was successful, {@code false otherwise}
     */
    boolean pull(@NotNull String targetId);

    /**
     * @param original  the older ICommit
     * @param compareTo the later ICommit
     * @return IFileDiff that contains the differences between the two files
     */
    @NotNull IFileDiff diff(@NotNull ICommit original, @NotNull ICommit compareTo);

    /**
     * @param url  the url from which to pull from, as String
     * @param dest the location on the local disk
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean clone(@NotNull String url, @NotNull File dest) throws IOException;

    /**
     * @return List of IFileStatus that describe the different staging states of the local files
     */
    @NotNull IFileStatus status();

    /**
     * @param files List of files that should be added to the .gitignore
     */
    void ignore(@NotNull List<File> files);

    /**
     * @param files List of files which should be reverted to the state in HEAD
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean revert(@NotNull List<File> files);

    /**
     * @param branchName String with the name of the branch
     * @param checkout   {@code true} if the branch should be automatically checked out after it was created
     * @return the identifier for the branch as String
     */
    @NotNull String createBranch(@NotNull String branchName, boolean checkout);

    /**
     * @param targetName String with identifier of the branch to checkout
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean checkout(@NotNull String targetName);

    /**
     * @param sourceName String with identifier of source branch
     * @param targetName String with identifier of target branch
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean canMerge(@NotNull String sourceName, String targetName);

    /**
     * @param sourceName String with identifier of source branch
     * @param targetName String with identifier of target branch
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean merge(@NotNull String sourceName, @NotNull String targetName);

    /**
     * @param identifier String with identifier of the commit
     * @return ICommit describing the commit
     */
    ICommit getCommit(String identifier);

    /**
     * @param sourceBranch IBranch for which all commits should be retrieved
     * @return List with all ICommits in the sourceBranch
     */
    List<ICommit> getCommits(IBranch sourceBranch);

    /**
     * @param forFile File for which all commits should be retrieved
     * @return List with ICommits that contains all commits that affected the file
     */
    List<ICommit> getCommits(File forFile);

    /**
     * @param identifier String with identifier of the targeted branch
     * @return the IBranch for the given identifier
     */
    IBranch getBranch(String identifier);

    /**
     * @return List of all IBranches in the repository
     */
    List<IBranch> getBranches();

}
