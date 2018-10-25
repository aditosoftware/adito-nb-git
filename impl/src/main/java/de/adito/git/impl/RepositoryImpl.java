package de.adito.git.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemChangeListener;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.impl.data.*;
import de.adito.git.impl.rxjava.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
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
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.adito.git.impl.Util.getRelativePath;

/**
 * @author A.Arnold 21.09.2018
 */
public class RepositoryImpl implements IRepository {

    private Git git;
    private final Observable<List<IBranch>> branchList;
    private final Observable<IFileStatus> status;

    @Inject
    public RepositoryImpl(IFileSystemObserverProvider pFileSystemObserverProvider, @Assisted IRepositoryDescription pRepositoryDescription) throws IOException {
        git = new Git(GitRepositoryProvider.get(pRepositoryDescription));
        branchList = BehaviorSubject.createDefault(_branchList());

        // listen for changes in the fileSystem for the status command
        status = Observable.create(new _FileSystemChangeObservable(pFileSystemObserverProvider.getFileSystemObserver(pRepositoryDescription))).startWith(_status());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(List<File> addList) throws Exception {
        AddCommand adder = git.add();
        for (File file : addList) {
            adder.addFilepattern(getRelativePath(file, git));
        }
        try {
            adder.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to add Files to staging area", e);
        }
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
    public boolean push() {
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
                    if (!diffEntry.getNewPath().equals("/dev/null"))
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
    public @NotNull Observable<IFileStatus> getStatus() {
        return status;
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
    public void revertWorkDir(@NotNull List<File> files) throws Exception {
        CheckoutCommand checkoutCommand = git.checkout();
        for(File file: files) {
            checkoutCommand.addPath(Util.getRelativePath(file ,git));
        }
        checkoutCommand.call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createBranch(@NotNull String branchName, boolean checkout) throws Exception {
        try {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

            if (refs.stream().anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName))) {
                throw new Exception("Branch already exist. " + branchName);
            }

            git.branchCreate().setName(branchName).call();
            // the next line of code is for an automatically push after creating a branch
            //git.push().setRemote("origin").setRefSpecs(new RefSpec().setSourceDestination(branchName, branchName)).call();
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
        CheckoutCommand checkout = git.checkout();
        checkout.setName(branchName).setStartPoint(branchName).setCreateBranch(false);
        try {
            checkout.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to checkout Branch: " + branchName, e);
        }
    }

    @Override
    public void checkout(@NotNull IBranch branch) throws Exception {
        CheckoutCommand checkout = git.checkout().setName(branch.getName()).setCreateBranch(false).setStartPoint(branch.getName());
        try {
            checkout.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to checkout Branch " + branch.getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IMergeDiff> merge(@NotNull String parentBranch, @NotNull String branchToMerge) throws
            Exception {
        List<IMergeDiff> mergeConflicts = new ArrayList<>();
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
                    .setCommit(false)
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF).call();
            if (mergeResult.getConflicts() != null) {
                RevCommit forkCommit = findForkPoint(parentBranch, branchToMerge);
                if (forkCommit != null)
                    mergeConflicts = _getMergeConflicts(parentBranch, branchToMerge, new CommitImpl(forkCommit), mergeResult.getConflicts());
            }
        } catch (GitAPIException e) {
            throw new Exception("Unable to execute the merge command: " + parentBranch + "and " + branchToMerge, e);
        }
        return mergeConflicts;
    }

    private List<IMergeDiff> _getMergeConflicts(String parentBranch, String branchToMerge, CommitImpl forkCommit, Map<String, int[][]> conflicts) throws Exception {
        List<IMergeDiff> mergeConflicts = new ArrayList<>();
        ICommit parentBranchCommit = null;
        ICommit toMergeCommit = null;
        try {
            parentBranchCommit = new CommitImpl(git.getRepository().parseCommit(git.getRepository().resolve(parentBranch)));
            toMergeCommit = new CommitImpl(git.getRepository().parseCommit(git.getRepository().resolve(branchToMerge)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (parentBranchCommit != null && toMergeCommit != null) {
            List<IFileDiff> parentDiffList = diff(parentBranchCommit, forkCommit);
            List<IFileDiff> toMergeDiffList = diff(toMergeCommit, forkCommit);
            for (IFileDiff parentDiff : parentDiffList) {
                if (conflicts.keySet().contains(parentDiff.getFilePath(EChangeSide.NEW))) {
                    for (IFileDiff toMergeDiff : toMergeDiffList) {
                        if (toMergeDiff.getFilePath(EChangeSide.NEW).equals(parentDiff.getFilePath(EChangeSide.NEW))) {
                            mergeConflicts.add(new MergeDiffImpl(parentDiff, toMergeDiff));
                        }
                    }
                }
            }
        }
        return mergeConflicts;
    }

    /**
     * @param parentBranchName  name of the branch currently on
     * @param foreignBranchName name of the branch for which to find the closest forkPoint to this branch
     * @return RevCommit that is the forkpoint between the two branches, or null if not available
     * @throws IOException if an error occurs during parsing
     */
    @Nullable
    private RevCommit findForkPoint(String parentBranchName, String foreignBranchName) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit foreignCommit = walk.lookupCommit(git.getRepository().resolve(foreignBranchName));
            List<ReflogEntry> refLog = git.getRepository().getReflogReader(parentBranchName).getReverseEntries();
            if (refLog.isEmpty()) {
                return null;
            }
            // <= to check both new and old ID for the oldest entry
            for (int index = 0; index <= refLog.size(); index++) {
                ObjectId commitId = index < refLog.size() ? refLog.get(index).getNewId() : refLog.get(index - 1).getOldId();
                RevCommit commit = walk.lookupCommit(commitId);
                // check if foreignCommit is reachable from the currently selected commit
                if (walk.isMergedInto(commit, foreignCommit)) {
                    // check if commit is a valid commit that does not contain errors when parsed
                    walk.parseBody(commit);
                    return commit;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommit getCommit(@NotNull String identifier) throws Exception {
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
        Iterable<RevCommit> refCommits;
        LogCommand logCommand = git.log().add(git.getRepository().resolve(sourceBranch.getName()));

        try {
            refCommits = logCommand.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to check the commits of one branch: " + sourceBranch, e);
        }
        if (refCommits != null) {
            refCommits.forEach(commit -> commitList.add(new CommitImpl(commit)));
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
        LogCommand logCommand = git.log().addPath(getRelativePath(file, git));

        try {
            logs = logCommand.call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to check the Commits of the File: " + file, e);
        }
        logs.forEach(log -> commitList.add(new CommitImpl(log)));
        return commitList;
    }

    @Override
    public String getDirectory()  {

        return String.valueOf(git.getRepository().getDirectory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getAllCommits() throws Exception {
        List<ICommit> commits = new ArrayList<>();
        Iterable<RevCommit> implCommits;
        LogCommand logCommand = git.log().all();
        try {
            implCommits = logCommand.call();
        } catch (GitAPIException e) {
            throw new Exception("Can't check the comments.", e);
        }
        if (implCommits != null) {
            implCommits.forEach(commit -> commits.add(new CommitImpl(commit)));
        }
        return commits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBranch getBranch(@NotNull String branchString) throws Exception {
        List<Ref> refList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        if (refList.isEmpty()) {
            throw new Exception("This Branch doesn't exists: " + branchString);
        }
        Ref branch = refList.stream().filter(pBranch -> pBranch.getName().equals(branchString))
                .collect(Collectors.toList()).get(0);

        return new BranchImpl(branch);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Observable<List<IBranch>> getBranches() {
        return branchList;
    }

    private List<IBranch> _branchList() {
        ListBranchCommand listBranchCommand = git.branchList().setListMode(ListBranchCommand.ListMode.ALL);
        List<IBranch> branchList = new ArrayList<>();
        List<Ref> refBranchList = null;
        try {
            refBranchList = listBranchCommand.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (refBranchList != null) {
            refBranchList.forEach(branch -> branchList.add(new BranchImpl(branch)));
        }
        return branchList;
    }

    private IFileStatus _status() {
        StatusCommand statusCommnand = git.status();
        Status currentStatus = null;
        try {
            currentStatus = statusCommnand.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return new FileStatusImpl(currentStatus);
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

    /**
     * Bridge from the FileSystemChangeListener to Observables
     */
    private class _FileSystemChangeObservable extends AbstractListenerObservable<IFileSystemChangeListener, IFileSystemObserver, IFileStatus> {

        _FileSystemChangeObservable(@NotNull IFileSystemObserver pListenableValue) {
            super(pListenableValue);
        }

        @NotNull
        @Override
        protected IFileSystemChangeListener registerListener(@NotNull IFileSystemObserver pListenableValue, @NotNull Consumer<IFileStatus> pOnNext) {
            IFileSystemChangeListener listener = () -> {
                pOnNext.accept(_status());
                System.out.println("set");
            };
            pListenableValue.addListener(listener);
            System.out.println("added listener");
            return listener;
        }

        @Override
        protected void removeListener(@NotNull IFileSystemObserver pListenableValue, @NotNull IFileSystemChangeListener pLISTENER) {
            System.out.println("removed listener");
            pListenableValue.removeListener(pLISTENER);
        }
    }
}