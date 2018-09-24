package de.adito.git.api;

import de.adito.git.api.data.IFileChange;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.IFileStatus;
import org.jetbrains.annotations.*;

import java.io.File;
import java.util.List;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IGit {

    boolean add(List<File> addList);

    String commit(@NotNull String message);

   //String commit(String message, Author author, Commiter commiter, List<File> files);

    boolean push();

    //boolean push(List<Commit> commits)

    @Nullable List<File> pull();

    @NotNull IFileDiff diff(@NotNull File original, @NotNull File compareTo);

    boolean clone(@NotNull String url, @NotNull File dest);

    @NotNull List<IFileStatus> status();

    void ignore(@NotNull List<File> files);

    boolean revert(@NotNull List<File> files);

    @NotNull List<IFileChange> fileHistory();

    @NotNull String createBranch(@NotNull String branchName, boolean checkout);

    boolean checkout(@NotNull String targetName);

    boolean canMerge(@NotNull String sourceName, String targetName);

    boolean merge(@NotNull String sourceName, @NotNull String targetName);
}
