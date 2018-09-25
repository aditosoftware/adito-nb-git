package de.adito.git.api;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IGit {

    boolean add(List<File> addList) throws GitAPIException;

    String commit(@NotNull String message);

   //String commit(String message, Author author, Commiter commiter, List<File> files);

    boolean push();

    //boolean push(List<Commit> commits)

    public boolean pull(@NotNull File localPath) throws IOException, GitAPIException;

    @NotNull IFileDiff diff(@NotNull File original, @NotNull File compareTo);

    boolean clone(@NotNull String url, @NotNull File localPath) throws GitAPIException, IOException;

    @NotNull List<IFileStatus> status();

    void ignore(@NotNull List<File> files);

    boolean revert(@NotNull List<File> files);

    @NotNull List<IFileHistory> fileHistory();

    @NotNull String createBranch(@NotNull String branchName, boolean checkout);

    boolean checkout(@NotNull String targetName);

    boolean canMerge(@NotNull String sourceName, String targetName);

    boolean merge(@NotNull String sourceName, @NotNull String targetName);

    ICommit getCommit(String identifier);

    List<ICommit> getCommits();

    List<ICommit> getCommits(IBranch sourceBranch);

    List<ICommit> getCommits(File forFile);

    IBranch getBranch(String identifier);

    List<IBranch> getBranches();

}
