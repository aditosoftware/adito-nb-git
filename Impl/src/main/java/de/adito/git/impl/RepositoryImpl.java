package de.adito.git.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.impl.data.BranchImpl;
import de.adito.git.impl.data.CommitImpl;
import de.adito.git.impl.data.FileDiffImpl;
import de.adito.git.impl.data.FileStatusImpl;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

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
        List<File> equalList = new ArrayList<>();
        Set<String> added = status().getAdded();

        if (addList.isEmpty()) {
            return true;
        }
        for (File file : addList) {
            AddCommand adder = git.add();
            adder.addFilepattern(getRelativePath(file, git));
            if (added.contains(getRelativePath(file, git)))
                equalList.add(file);
            try {
                adder.call();
            } catch (GitAPIException e) {
                throw new Exception("Unable to add Files to staging area", e);
            }
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

    @Override
    public @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) throws Exception {
        List<IFileDiff> listDiffImpl = new ArrayList<>();

        ObjectId head = git.getRepository().resolve(original.getShortMessage());
        ObjectId prevHead = git.getRepository().resolve(compareTo.getShortMessage());
        List<DiffEntry> listDiff;

        ObjectReader reader = git.getRepository().newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, prevHead);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, head);

        try {
            listDiff = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to show changes between commits");
        }

        if (listDiff != null) {
            for (DiffEntry diffs : listDiff) {
                listDiffImpl.add(new FileDiffImpl(diffs));
            }
        }
        return listDiffImpl;
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
    public boolean revert(@NotNull List<File> files) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String createBranch(@NotNull String branchName, boolean checkout) throws Exception {
        boolean alreadyExists = false;
        try {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : refs) {
                if (ref.getName().equals("refs/heads/" + branchName)) {
                    alreadyExists = true;
                    throw new Exception("Branch name already exists. " + branchName);
                }
            }
            if (!alreadyExists) {
                git.branchCreate()
                        .setName(branchName)
                        .call();
                git.push().setRemote("origin")
                        .setRefSpecs(new RefSpec().setSourceDestination(branchName, branchName)).call();
                alreadyExists = false;
            }
            if (checkout) {
                checkout(branchName);
            }
        } catch (GitAPIException e) {
            throw new Exception("Unable to create new branch: " + branchName, e);
        }
        return "Created and Checkout: " + branchName;
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
    public boolean checkout(@NotNull String branchName) throws Exception {
        CheckoutCommand check = git.checkout();
        check.setName(branchName).setStartPoint(branchName).setCreateBranch(false);
        try {
            check.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to checkout the Branch: " + branchName, e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(@NotNull String parentBranch, @NotNull String branchToMerge, @NotNull String commitMessage) throws Exception {
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
        Iterable<RevCommit> commits;
        try {
            commits = git.log().add(ObjectId.fromString(identifier)).call();
        } catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
            throw new Exception("Can't check the commits.", e);
        }
        if (commits != null) {
            Iterator<RevCommit> iterator = commits.iterator();
            if (iterator.hasNext()) {
                commit = iterator.next();
                if (iterator.hasNext())
                    throw new Exception("There are more Commits with one identifier: " + identifier);
            } else throw new Exception("There are no commit with this identifier: " + identifier);
        } else throw new Exception("There are no commit with this identifier: " + identifier);
        return new CommitImpl(commit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) throws Exception {
        List<ICommit> commitList = new ArrayList();
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
    public IBranch getBranch(String identifier) throws Exception {
        Ref branch = null;
        List<Ref> branches = new ArrayList<>();
        try {
            branches = git.branchList().setContains(identifier).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (branches != null) {
            Iterator<Ref> iterator = branches.iterator();
            if (iterator.hasNext()) {
                branch = iterator.next();
                if (iterator.hasNext())
                    throw new Exception("There are more than one branch for this identifier: " + identifier);
                else throw new Exception("There are more than one branch for this identifier: " + identifier);
            } else throw new Exception("There are more than one branch for this identifier: " + identifier);
        }
        return new BranchImpl(branch);
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
}