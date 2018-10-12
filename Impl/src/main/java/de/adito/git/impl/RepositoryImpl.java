package de.adito.git.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.impl.data.BranchImpl;
import de.adito.git.impl.data.CommitImpl;
import de.adito.git.impl.data.FileDiffImpl;
import de.adito.git.impl.data.FileStatusImpl;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.adito.git.impl.Util.getRelativePath;

/**
 * @author A.Arnold 21.09.2018
 */

public class RepositoryImpl implements IRepository {

    private Git git;

    @Inject
    public RepositoryImpl(@Assisted String repoPath) throws IOException {
        git = new Git(GitRepositoryProvider.get(repoPath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(List<File> addList) throws Exception {
        if (addList.isEmpty()) {
            return true;
        }
        AddCommand adder = git.add();
        for (File file : addList) {
            adder.addFilepattern(getRelativePath(file, git));
        }
        try {
            adder.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to add Files to staging area", e);
        }
        Set<String> added = status().getAdded();
        for (File fileToAdd : addList) {
            if (!added.contains(getRelativePath(fileToAdd, git)))
                return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String commit(@NotNull String message) throws Exception {
        CommitCommand commit = git.commit();
        RevCommit revCommit;
        try {
            revCommit = commit.setMessage(message).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to commit to local Area", e);
        }
        if (revCommit == null) {
            return "";
        }
        return ObjectId.toString(revCommit.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String commit(@NotNull String message, List<File> fileList) throws Exception {
        CommitCommand commit = git.commit();
        RevCommit revCommit;
        for (File file : fileList) {
            commit.setOnly(getRelativePath(file, git));
        }
        try {
            revCommit = commit.setMessage(message).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to commit to local Area", e);
        }
        if (revCommit == null) {
            return "";
        }
        return ObjectId.toString(revCommit.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean push() throws Exception {
        if (status().getAdded().isEmpty()) {
            throw new Exception("there are no Files to push!");
        }
        PushCommand push = git.push()
                .setTransportConfigCallback(new TransportConfigCallbackImpl(null, null));
        try {
            push.call();
        } catch (JGitInternalException | GitAPIException e) {
            throw new IllegalStateException("Unable to push into remote Git repository", e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pull(@NotNull String targetId) throws Exception {
        if (targetId.equals("")) {
            targetId = "master";
        }
        PullCommand pullcommand = git.pull().setRemoteBranchName(targetId);
        pullcommand.setTransportConfigCallback(new TransportConfigCallbackImpl(null, null));
        try {
            pullcommand.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to pull new files", e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) throws Exception {
        List<IFileDiff> listDiffImpl = new ArrayList<>();

        List<DiffEntry> listDiff;

        CanonicalTreeParser oldTreeIter = prepareTreeParser(git.getRepository(), ObjectId.fromString(compareTo.getId()));
        CanonicalTreeParser newTreeIter = prepareTreeParser(git.getRepository(), ObjectId.fromString(original.getId()));

        try {
            listDiff = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to show changes between commits");
        }

        if (listDiff != null) {
            for (DiffEntry diff : listDiff) {
                try (DiffFormatter formatter = new DiffFormatter(null)) {
                    formatter.setRepository(GitRepositoryProvider.get());
                    FileHeader fileHeader = formatter.toFileHeader(diff);
                    listDiffImpl.add(new FileDiffImpl(diff, fileHeader, getFileContents(getFileVersion(compareTo.getId(), diff.getOldPath())), getFileContents(getFileVersion(original.getId(), diff.getNewPath()))));
                }
            }
        }
        return listDiffImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<IFileDiff> diff(@Nullable List<File> filesToDiff) throws Exception {
        List<IFileDiff> returnList = new ArrayList<>();

        // prepare the TreeIterators for the local working copy and the files in HEAD
        FileTreeIterator fileTreeIterator = new FileTreeIterator(git.getRepository());
        ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
        CanonicalTreeParser treeParser = prepareTreeParser(git.getRepository(), lastCommitId);

        // Use the DiffFormatter to retrieve a list of changes
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setRepository(git.getRepository());
        diffFormatter.setDiffComparator(RawTextComparator.WS_IGNORE_TRAILING);
        List<DiffEntry> diffList = diffFormatter.scan(treeParser, fileTreeIterator);

        for (DiffEntry diffEntry : diffList) {
            // check if the diff is of a file in  the passed list, except if filesToDiff is null (all files are valid).
            if (filesToDiff == null
                    || filesToDiff.stream().anyMatch(file -> getRelativePath(file, git).equals(diffEntry.getNewPath()))
                    || filesToDiff.stream().anyMatch(file -> getRelativePath(file, git).equals(diffEntry.getOldPath()))) {
                FileHeader fileHeader = diffFormatter.toFileHeader(diffEntry);
                // remark: there seems to always be only one Hunk in the file header
                if (fileHeader.getHunks().get(0).toEditList().size() > 0) {
                    // Can't use the ObjectLoader or anything similar provided by JGit because it wouldn't find the blob, so parse file by hand
                    StringBuilder newFileLines = new StringBuilder();
                    if(!diffEntry.getNewPath().equals("/dev/null"))
                        Files.lines(new File(diffEntry.getNewPath()).toPath()).forEach(line -> newFileLines.append(line).append("\n"));
                    String oldFileContents = diffEntry.getOldPath().equals("/dev/null") ? "" : getFileContents(getFileVersion(ObjectId.toString(lastCommitId), diffEntry.getOldPath()));
                    returnList.add(new FileDiffImpl(diffEntry, fileHeader,
                            oldFileContents, newFileLines.toString()));
                }
            }
        }
        return returnList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileContents(String identifier) throws IOException {
        ObjectLoader loader = git.getRepository().open(ObjectId.fromString(identifier));
        return new String(loader.getBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileVersion(String commitId, String filename) throws IOException {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitId));
            RevTree tree = commit.getTree();

            // find the specific file
            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filename));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Could not find file " + filename);
                }
                return ObjectId.toString(treeWalk.getObjectId(0));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clone(@NotNull String url, @NotNull File localPath) {

        if (Util.isDirEmpty(localPath)) {
            CloneCommand cloneRepo = Git.cloneRepository()
                    // TODO: 25.09.2018 NetBeans pPassword
                    .setTransportConfigCallback(new TransportConfigCallbackImpl(null, null))
                    .setURI(url)
                    .setDirectory(new File(localPath, ""));
            try {
                cloneRepo.call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IFileStatus status() {
        StatusCommand status = git.status();
        Status statusImpl = null;
        try {
            statusImpl = status.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return new FileStatusImpl(statusImpl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignore(@NotNull List<File> files) throws IOException {
        File gitIgnore = new File(GitRepositoryProvider.get().getDirectory().getParent(), ".gitignore");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true))) {
            for (File file : files) {
                outputStream.write((getRelativePath(file, git) + "\n").getBytes());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exclude(@NotNull List<File> files) throws IOException {
        File gitIgnore = new File(GitRepositoryProvider.get().getDirectory(), "info/exclude");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true))) {
            for (File file : files) {
                outputStream.write((getRelativePath(file, git) + "\n").getBytes());
            }
        }
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
    public void createBranch(@NotNull String branchName, boolean checkout) throws Exception {
        try {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : refs) {
                if (ref.getName().equals("refs/heads/" + branchName)) {
                    throw new Exception("Branch name already exists. " + branchName);
                }
            }
            git.branchCreate()
                    .setName(branchName)
                    .call();
            git.push().setRemote("origin")
                    .setRefSpecs(new RefSpec().setSourceDestination(branchName, branchName)).call();
            if (checkout) {
                checkout(branchName);
            }
        } catch (GitAPIException e) {
            throw new Exception("Unable to create new branch: " + branchName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBranch(@NotNull String branchName) throws Exception {
        String destination = "refs/heads/" + branchName;
        try {
            git.branchDelete()
                    .setBranchNames(destination)
                    .call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to delete the branch: " + branchName, e);
        }
        RefSpec refSpec = new RefSpec().setSource(null).setDestination(destination);
        try {
            git.push().setRefSpecs(refSpec).setRemote("origin").call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to push the delete branch comment @ " + branchName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkout(@NotNull String branchName) throws Exception {
        CheckoutCommand check = git.checkout();
        check.setName(branchName).setStartPoint(branchName).setCreateBranch(false);
        try {
            check.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to checkout the Branch: " + branchName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(@NotNull String parentBranch, @NotNull String branchToMerge, @NotNull String commitMessage) throws
            Exception {
        try {
            checkout(parentBranch);
        } catch (Exception e) {
            throw new Exception("Unable to checkout the parentBranch: " + parentBranch + "at the merge command");
        }
        ObjectId mergeBase;
        try {
            mergeBase = git.getRepository().resolve(branchToMerge);
        } catch (IOException e) {
            throw new Exception("Unable to merge the branch " + branchToMerge + " and " + parentBranch, e);
        }
        try {
            MergeResult mergeResult = git.merge()
                    .include(mergeBase)
                    .setCommit(true)
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF).setMessage(commitMessage).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to execute the merge command: " + parentBranch + "and " + branchToMerge, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommit getCommit(String identifier) throws Exception {
        RevCommit commit;
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            ObjectId commitId = ObjectId.fromString(identifier);
            commit = revWalk.parseCommit(commitId);
        }
        return new CommitImpl(commit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) throws Exception {
        List<ICommit> commitList = new ArrayList<>();
        Iterable<RevCommit> commits;
        try {
            commits = git.log().add(git.getRepository().resolve(sourceBranch.getName())).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to check the commits of one branch: " + sourceBranch, e);
        }
        if (commits != null) {
            for (RevCommit commit : commits) {
                commitList.add(new CommitImpl(commit));
            }
        } else {
            throw new Exception("The branch can't be empty: " + sourceBranch);
        }
        return commitList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(File file) throws Exception {
        List<ICommit> commitList = new ArrayList<>();
        Iterable<RevCommit> logs;
        try {
            logs = git.log().addPath(getRelativePath(file, git)).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to check the Commits of the File: " + file, e);
        }
        for (RevCommit log : logs) {
            commitList.add(new CommitImpl(log));
            System.out.println(log);
        }
        return commitList;
    }

    @Override
    public List<ICommit> getAllCommits() throws Exception {
        Iterable<RevCommit> oldCommitList;
        List<ICommit> allCommits = new ArrayList<>();

        try {
            oldCommitList = git.log().all().call();
        } catch (GitAPIException e) {
            throw new Exception("Can't check the comments.", e);
        }
        if (oldCommitList != null) {
            for (RevCommit commit : oldCommitList) {
                allCommits.add(new CommitImpl(commit));
            }
        }
        return allCommits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBranch getBranch(@NotNull String branchString) throws Exception {
        List<Ref> branches = git.branchList().call();
        if (branches.isEmpty()) {
            throw new Exception("This Branch doesn't exists: " + branchString);
        }
        for (Ref branch : branches) {
            if (branch.getName().equals(branchString)) {
                return new BranchImpl(branch);
            }
        }
        return new BranchImpl(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IBranch> getBranches() throws Exception {
        List<Ref> refList;
        List<IBranch> branches = new ArrayList<>();
        try {
            refList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        } catch (GitAPIException e) {
            throw new Exception("Can't call branches from Remote", e);
        }
        if (refList != null) {
            for (Ref ref : refList) {
                branches.add(new BranchImpl(ref));
            }

        }
        return branches;
    }

    /**
     * Helperfunction to prepare the TreeParser for the diff function
     *
     * @param repository the (git) repository
     * @param objectId   the objectId for the commit/Branch that the Tree should be prepared for
     * @return initialised CanonicalTreeParser
     * @throws IOException if an error occurs, such as an invalid ID or the treeparser cannot be reset
     */
    private CanonicalTreeParser prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(objectId);
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
}