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
import de.adito.git.data.FileStatusImpl;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    public List add(List<File> addList) {
        if (addList.isEmpty()) {
            return addList;
        }
        for (File file : addList) {
            AddCommand adder = git.add();
            adder.addFilepattern(getRelativePath(file, git));
            try {
                adder.call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
        //FIXME: just returning the list you got as parameter makes no sense here. Check if the files were really added to staging and remove any that were not
        return addList;
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
        //FIXME: check revCommit for null, if yes return emtpy string
        return ObjectId.toString(revCommit.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean push(@NotNull String targetId) {
        if (status().getAdded().isEmpty()) {
            // TODO: 26.09.2018 Throw Netbeans dialog
        } else {
            PushCommand push = git.push();
            push.setTransportConfigCallback(new TransportConfigCallbackImpl(null, null));
            try {
                push.call();
            } catch (JGitInternalException | GitAPIException e) {
                throw new IllegalStateException("Unable to push into remote Git repository", e);
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pull(@NotNull String targetId) {
        //FIXME: @NotNull and check for null makes no real sense. Maybe check for "" instead, or remove @NotNull
        if (targetId == null) {
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
    public @NotNull IFileDiff diff(@NotNull ICommit original, @NotNull ICommit compareTo) {
        // TODO: 26.09.2018

        IFileDiff diff = null;
/*        try {
            diff = new FileDiffImpl(git.diff(original, compareTo));
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
*/
        return diff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clone(@NotNull String url, @NotNull File localPath) {

        try {
            if (Util.isDirEmpty(localPath)) {
                Git git = new Git(GitRepositoryProvider.get(localPath.getAbsolutePath() + File.separator + ".git"));
                try {
                    //FIXME: see IntelliJ warning
                    git.cloneRepository()
                            // TODO: 25.09.2018 NetBeans pPassword
                            .setTransportConfigCallback(new TransportConfigCallbackImpl(null, null))
                            .setURI(url)
                            .setDirectory(new File(localPath, ""))
                            .call();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }
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
    public @NotNull IFileStatus status() {

        //FIXME: swapping the variable names would make more sense
        StatusCommand status = git.status();
        Status call = null;
        try {
            call = status.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return new FileStatusImpl(call);
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
        try {
            boolean alreadyExists = false;
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : refs) {
                System.out.println("DEBUG: Branchname: " + ref.getName());
                if (ref.getName().equals("refs/heads/" + branchName)) {
                    //FIXME: setting a local variable just before jumping out of the method via return makes no sense
                    alreadyExists = true;
                    return "Branch already exists"; // TODO: 26.09.2018 NB Task
                }
            }
            //FIXME: unnecessary, alreadyExists is always false (you jump out of the method right after the only place where it is set true)
            if (!alreadyExists) {
                git.branchCreate()
                        .setName(branchName)
                        .call();
                git.push().setRemote("origin")
                        .setRefSpecs(new RefSpec().setSourceDestination(branchName, branchName)).call();
                //FIXME: why?
                alreadyExists = false;
            }
            //FIXME: == instead of =
            if (checkout = true) {
                checkout(branchName);
            }
        } catch (GitAPIException e) {
            e.printStackTrace(); // TODO: 26.09.2018 NB Task
        }
        //FIXME: only return the id of the branch, don't add any unnecessary stuff
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
        check.setName(branchName).setStartPoint(branchName);
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
    public boolean merge(@NotNull String sourceName, @NotNull String targetName) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommit getCommit(String identifier) {
        RevCommit commit = null;
        Iterable<RevCommit> iterable = null;
        try {
            iterable = git.log().add(ObjectId.fromString(identifier)).call();
        } catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
            e.printStackTrace();
        }
        //FIXME: check with hasNext() if there is an item available first
        commit = iterable.iterator().next();

        if (iterable.iterator().hasNext()) {
            // TODO: 26.09.2018 NB Task
        }
        return new CommitImpl(commit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) throws IOException {
        // TODO: 26.09.2018 Branch ID
        //FIXME: don't use generic list, tell the list which class it has to expect
        List list = new ArrayList();

        try {
            for (RevCommit commit : git.log().add(git.getRepository().resolve(sourceBranch.getName())).call()) {
                list.add(commit);
            }
            return list;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        //FIXME: should probably return null here, only reachable after an exception happened
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(File forFile) {
        // TODO: 26.09.2018 TreeIterator -> Changes at one File
        git.log().addPath(getRelativePath(forFile, git));
        //FIXME: ?
        return null;
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

        //FIXME: check with hasNext() if item is available first
        branch = list.iterator().next();
        if (list.iterator().hasNext()) {
            // TODO: 26.09.2018 NB Task
        }
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
        //FIXME: check if refList is null first, or initialize refList with new ArrayList<>() up  top
        while (refList.iterator().hasNext()) {
            branches.add(new BranchImpl(refList.iterator().next()));
        }
        return branches;
    }
}

