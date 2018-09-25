package de.adito.git;

import de.adito.git.api.*;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.adito.git.Util.getRelativePath;

/**
 * @author A.Arnold 21.09.2018
 */

public class GitImpl implements IGit {


    private Git git;

    public GitImpl(Git pGit) {
        git = pGit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(List<File> addList) throws GitAPIException {
        if (addList.isEmpty()) {
            return false;
        }
        for (File file : addList) {
            AddCommand adder = git.add();
            adder.addFilepattern(getRelativePath(file, git)).call();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String commit(@NotNull String message) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean push() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pull(@NotNull File localPath) throws IOException, GitAPIException {

        try {
            // TODO: 25.09.2018 Doesn't work now
            //Repository localRepostitory = new FileRepository(localPath.getAbsolutePath() + File.separator + ".git");
            git.getRepository().getDirectory();
            Git git = new Git(RepositoryProvider.get(localPath.getAbsolutePath() + File.separator + ".git"));
            git.pull().call();
            return true;
        } catch (IOException e) {
            // TODO: 25.09.2018 NB Exception catching
            System.out.println(e.getMessage());
        }


        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IFileDiff diff(@NotNull File original, @NotNull File compareTo) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clone(@NotNull String url, @NotNull File localPath) throws GitAPIException, IOException {

        try {
            if (Util.isDirEmpty(localPath)) {
                Git git = new Git(RepositoryProvider.get(localPath.getAbsolutePath() + File.separator + ".git"));
                git.cloneRepository()
                        .setTransportConfigCallback(new TransportConfigCallbackImpl(null, null))
                        .setURI(url)
                        .setDirectory(new File(localPath, ""))
                        .call();
                return true;
            }
        } catch (IOException e) {
            // TODO: 25.09.2018 NB Exception catching
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<IFileStatus> status() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignore(@NotNull List<File> files) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean revert(@NotNull List<File> files) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<IFileHistory> fileHistory() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String createBranch(@NotNull String branchName, boolean checkout) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkout(@NotNull String targetName) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canMerge(@NotNull String sourceName, String targetName) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean merge(@NotNull String sourceName, @NotNull String targetName) {
        return false;
    }

    @Override
    public ICommit getCommit(String identifier) {
        return null;
    }

    @Override
    public List<ICommit> getCommits() {
        return null;
    }

    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) {
        return null;
    }

    @Override
    public List<ICommit> getCommits(File forFile) {
        return null;
    }

    @Override
    public IBranch getBranch(String identifier) {
        return null;
    }

    @Override
    public List<IBranch> getBranches() {
        return null;
    }

    //doesn't needed now
    private void _addAllFiles() throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.add().setUpdate(true).addFilepattern(".").call();
    }
}

