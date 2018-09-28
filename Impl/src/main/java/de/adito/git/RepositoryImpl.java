package de.adito.git;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.data.BranchImpl;
import de.adito.git.data.CommitImpl;
import de.adito.git.data.FileDiffImpl;
import de.adito.git.data.FileStatusImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.adito.git.Util.getRelativePath;

/**
 * @author A.Arnold 21.09.2018
 */

public class RepositoryImpl implements IRepository {

    private Git git;

//    GitImpl(Git pGit) {
//        git = pGit;
//    }

    @Inject
    public RepositoryImpl(@Assisted String repoPath) throws IOException {
        git = new Git(GitRepositoryProvider.get(repoPath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> add(List<File> addList) {
        List<File> equalList = new ArrayList<>();
        Set<String> added = status().getAdded();

        if (addList.isEmpty()) {
            return addList;
        }
        for (File file : addList) {
            AddCommand adder = git.add();
            adder.addFilepattern(getRelativePath(file, git));

            if (added.contains(getRelativePath(file, git)))
                equalList.add(file);
            try {
                adder.call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
        return equalList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String commit(@NotNull String message) {
        CommitCommand commit = git.commit();
        RevCommit revCommit = null;
        try {
            revCommit = commit.setMessage(message).call();
        } catch (GitAPIException e) {
            e.printStackTrace();// TODO: 26.09.2018 NB Task
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
    public boolean push() {
        if (status().getAdded().isEmpty()) {
            // TODO: 26.09.2018 Throw Netbeans dialog
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
    public boolean pull(@NotNull String targetId) {
        if (targetId.equals("")) {
            targetId = "master";
        }
        PullCommand pullcommand = git.pull().setRemoteBranchName(targetId);
        pullcommand.setTransportConfigCallback(new TransportConfigCallbackImpl(null, null));
        try {
            pullcommand.call();
        } catch (GitAPIException e) {
            e.printStackTrace(); // TODO: 26.09.2018 NB Task
        }
        return true;
    }

    @Override
    public @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) throws IOException {
        // TODO: 26.09.2018
        List<IFileDiff> listDiffImpl = new ArrayList<>();

        ObjectId head = git.getRepository().resolve(original.getShortMessage());
        ObjectId prevHead = git.getRepository().resolve(compareTo.getShortMessage());
        List<DiffEntry> listDiff = null;

        ObjectReader reader = git.getRepository().newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, prevHead);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, head);

        try {
            listDiff = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (listDiff != null) {
            for(DiffEntry diffs : listDiff){
                listDiffImpl.add( new FileDiffImpl(diffs));
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
        try(BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true))){
            for(File file: files) {
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
    public @NotNull String createBranch(@NotNull String branchName, boolean checkout) {
        boolean alreadyExists = false;
        try {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : refs) {
                System.out.println("DEBUG: Branchname: " + ref.getName());
                if (ref.getName().equals("refs/heads/" + branchName)) {
                    alreadyExists = true;
                    return "Branch already exists"; // TODO: 26.09.2018 NB Task
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
            e.printStackTrace(); // TODO: 26.09.2018 NB Task
        }
        return "Created and Checkout: " + branchName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBranch(@NotNull String branchName) {
        String destination = "refs/heads/" + branchName;
        try {
            git.branchDelete()
                    .setBranchNames(destination)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        RefSpec refSpec = new RefSpec().setSource(null).setDestination(destination);
        try {
            git.push().setRefSpecs(refSpec).setRemote("origin").call();
        } catch (GitAPIException e) { // TODO: 26.09.2018 NB Task
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkout(@NotNull String branchName) {
        CheckoutCommand check = git.checkout();
        check.setName(branchName).setStartPoint(branchName).setCreateBranch(false);
        try {
            check.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return true;
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
    public boolean merge(@NotNull String parentBranch, @NotNull String branchToMerge, @NotNull String commitMessage) {
        try {
            checkout(parentBranch);
            ObjectId mergeBase;
            mergeBase = git.getRepository().resolve(branchToMerge);
            MergeResult mergeResult = git.merge()
                    .include(mergeBase)
                    .setCommit(true)
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF).setMessage(commitMessage).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommit getCommit(String identifier) {
        RevCommit commit = null;
        Iterable<RevCommit> commits = null;
        try {
            commits = git.log().add(ObjectId.fromString(identifier)).call();
        } catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
            e.printStackTrace();
        }
        if (commits != null) {
            if (commits.iterator().hasNext()) {
                // TODO: 26.09.2018 NB Task
            }
            commit = commits.iterator().next();
        }  // else TODO: 27.09.2018 NB Task
        return new CommitImpl(commit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) throws IOException {
        // TODO: 26.09.2018 Branch ID
        List<ICommit> commitList = new ArrayList();
        Iterable<RevCommit> commits = null;
        try {
            commits = git.log().add(git.getRepository().resolve(sourceBranch.getName())).call();
            /*
            for (RevCommit commit : git.log().add(git.getRepository().resolve(sourceBranch.getName())).call()) {
                list.add(commit);
            }*/

        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (commits != null) {
            for (RevCommit commit : commits) {
                commitList.add(new CommitImpl(commits.iterator().next()));
            }
        }
        return commitList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(File forFile) throws IOException {
        List<ICommit> commitList = new ArrayList<>();
        Iterable<RevCommit> commits = null;
        try {
            commits = git.log().add(git.getRepository().resolve(getRelativePath(forFile, git))).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (commits != null) {
            for (RevCommit commit : commits) {
                commitList.add(new CommitImpl(commit));
            }
        }
        return commitList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBranch getBranch(String identifier) {
        Ref branch;

        List<Ref> list = new ArrayList<>();
        try {
            list = git.branchList().setContains(identifier).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (list.iterator().hasNext()) {
            // TODO: 26.09.2018 NB Task
        }
        branch = list.iterator().next();
        return new BranchImpl(branch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IBranch> getBranches() {
        List<Ref> refList = null;
        List<IBranch> branches = new ArrayList<>();
        try {
            refList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (refList != null) {
            while (refList.iterator().hasNext()) {
                branches.add(new BranchImpl(refList.iterator().next()));
            }
        }
        return branches;
    }
}

