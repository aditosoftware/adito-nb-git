package de.adito.git.api;

import de.adito.git.api.data.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * An empty repository object
 *
 * @author a.arnold, 05.11.2018
 */
class EmptyRepository implements IRepository {
    @Override
    public void add(List<File> addList) {
        throw new RuntimeException();
    }

    @Override
    public String commit(@NotNull String message) {
        throw new RuntimeException();
    }

    @Override
    public String commit(@NotNull String message, List<File> fileList) {
        throw new RuntimeException();
    }

    @Override
    public boolean push() {
        throw new RuntimeException();
    }

    @Override
    public boolean pull(@NotNull String targetId) {
        throw new RuntimeException();
    }

    @Override
    public void fetch(){
    }

    @Override
    public void fetch(boolean prune){
    }

    @Override
    public @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) {
        throw new RuntimeException();
    }

    @Override
    public @NotNull List<IFileDiff> diff(@Nullable List<File> fileToDiff, @Nullable ICommit compareWith){
        return Collections.emptyList();
    }

    @Override
    public String getFileContents(String identifier) {
        throw new RuntimeException();
    }

    @Override
    public String getFileVersion(String commitId, String filename) {
        throw new RuntimeException();
    }

    @Override
    public boolean clone(@NotNull String url, @NotNull File dest) {
        throw new RuntimeException();
    }

    @Override
    public @NotNull Observable<IFileStatus> getStatus() {
        throw new RuntimeException();
    }

    @Override
    public void ignore(@NotNull List<File> files) {
        throw new RuntimeException();
    }

    @Override
    public void exclude(@NotNull List<File> files) {
        throw new RuntimeException();
    }

    @Override
    public void revertWorkDir(@NotNull List<File> files) {
        throw new RuntimeException();
    }

    @Override
    public void reset(@NotNull List<File> files) {
        throw new RuntimeException();
    }

    @Override
    public void reset(@NotNull String identifier, @NotNull EResetType resetType) {
        throw new RuntimeException();
    }

    @Override
    public void createBranch(@NotNull String branchName, boolean checkout) {
        throw new RuntimeException();
    }

    @Override
    public void deleteBranch(@NotNull String branchName) {
        throw new RuntimeException();
    }

    @Override
    public void checkout(@NotNull String branchName) {
        throw new RuntimeException();
    }

    @Override
    public void checkout(@NotNull IBranch branch) {
        throw new RuntimeException();
    }

    @Override
    public List<IMergeDiff> getMergeConflicts() {
        throw new RuntimeException();
    }

    @Override
    public List<IMergeDiff> merge(@NotNull IBranch sourceBranch, @NotNull IBranch targetBranch){
        throw new RuntimeException();
    }

    @Override
    public List<String> getCommitedFiles(String commitId) {
        throw new RuntimeException();
    }

    @Override
    public ICommit getCommit(@NotNull String identifier) {
        throw new RuntimeException();
    }

    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) {
        throw new RuntimeException();
    }

    @Override
    public List<ICommit> getCommits(File forFile) {
        throw new RuntimeException();
    }

    @Override
    public List<CommitHistoryTreeListItem> getCommitHistoryTreeList(@NotNull List<ICommit> commits) {
        return null;
    }

    @Override
    public String getDirectory() {
        throw new RuntimeException();
    }

    @Override
    public File getTopLevelDirectory() {
        throw new RuntimeException();
    }

    @Override
    public List<ICommit> getAllCommits() {
        throw new RuntimeException();
    }

    @Override
    public IBranch getBranch(String branchString) {
        throw new RuntimeException();
    }

    @Override
    public Observable<IBranch> getCurrentBranch() {
        throw new RuntimeException();
    }

    @Override
    public @NotNull Observable<List<IBranch>> getBranches() {
        throw new RuntimeException();
    }
}
