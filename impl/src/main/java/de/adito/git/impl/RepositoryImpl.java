package de.adito.git.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
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

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    private final BehaviorSubject<IBranch> currentBranch;
    private final ColorRoulette colorRoulette;

    @Inject
    public RepositoryImpl(IFileSystemObserverProvider pFileSystemObserverProvider, ColorRoulette pColorRoulette, @Assisted IRepositoryDescription pRepositoryDescription) throws IOException {
        colorRoulette = pColorRoulette;
        git = new Git(GitRepositoryProvider.get(pRepositoryDescription));
        branchList = BehaviorSubject.createDefault(_branchList());

        // listen for changes in the fileSystem for the status command
        status = Observable.create(new _FileSystemChangeObservable(pFileSystemObserverProvider.getFileSystemObserver(pRepositoryDescription)))
                .subscribeWith(BehaviorSubject.createDefault(_status()));

        // Current Branch
        IBranch curBranch = _currentBranch();
        currentBranch = BehaviorSubject.createDefault(curBranch == null ? IBranch.EMPTY : curBranch);
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
        if (status.blockingFirst().hasUncommittedChanges()) {
            //TODO: remove this once stashing is implemented and do stash -> pull -> un-stash
            System.err.println("Not able to pull files from remote due to uncommitted, changed files. Either commit or revert them and try again");
            return false;
        }
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
    public void fetch() throws Exception {
        fetch(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetch(boolean prune) throws Exception {
        git.fetch().setRemoveDeletedRefs(prune).call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<IFileDiff> diff(@NotNull ICommit original, @NotNull ICommit compareTo) throws Exception {
        List<IFileDiff> listDiffImpl = new ArrayList<>();

        List<DiffEntry> listDiff = _doDiff(ObjectId.fromString(original.getId()), ObjectId.fromString(compareTo.getId()));

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
    public @NotNull List<IFileDiff> diff(@Nullable List<File> filesToDiff, @Nullable ICommit compareWith) throws Exception {
        List<IFileDiff> returnList = new ArrayList<>();

        // prepare the TreeIterators for the local working copy and the files in HEAD
        FileTreeIterator fileTreeIterator = new FileTreeIterator(git.getRepository());
        ObjectId compareWithId = git.getRepository().resolve(compareWith == null ? Constants.HEAD : compareWith.getId());
        CanonicalTreeParser treeParser = prepareTreeParser(git.getRepository(), compareWithId);

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
                        Files.lines(new File(getTopLevelDirectory(), diffEntry.getNewPath()).toPath()).forEach(line -> newFileLines.append(line).append("\n"));
                    String oldFileContents = diffEntry.getOldPath().equals("/dev/null") ? "" : getFileContents(getFileVersion(ObjectId.toString(compareWithId), diffEntry.getOldPath()));
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
        for (File file : files) {
            checkoutCommand.addPath(Util.getRelativePath(file, git));
        }
        checkoutCommand.call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(@NotNull List<File> files) throws Exception {
        ResetCommand resetCommand = git.reset();
        for (File file : files)
            resetCommand.addPath(Util.getRelativePath(file, git));
        resetCommand.call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(@NotNull String identifier, @NotNull EResetType resetType) throws Exception {
        ResetCommand resetCommand = git.reset();
        resetCommand.setRef(identifier);
        if (resetType == EResetType.HARD)
            resetCommand.setMode(ResetCommand.ResetType.HARD);
        else if (resetType == EResetType.MIXED)
            resetCommand.setMode(ResetCommand.ResetType.MIXED);
        else resetCommand.setMode(ResetCommand.ResetType.SOFT);
        resetCommand.call();
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
        checkout(getBranch(branchName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkout(@NotNull IBranch branch) throws Exception {
        CheckoutCommand checkout = git.checkout().setName(branch.getName()).setCreateBranch(false).setStartPoint(branch.getName());
        try {
            checkout.call();
            currentBranch.onNext(branch);
        } catch (GitAPIException e) {
            throw new Exception("Unable to checkout Branch " + branch.getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<IMergeDiff> getMergeConflicts() throws Exception {
        Set<String> conflictingFiles = status.blockingFirst().getConflicting();
        if (conflictingFiles.size() > 0) {
            File aConflictingFile = new File(git.getRepository().getDirectory().getParent(), conflictingFiles.iterator().next());
            String currentBranch = git.getRepository().getBranch();
            String branchToMerge = _getConflictingBranch(aConflictingFile);
            return _getMergeConflicts(currentBranch, branchToMerge, new CommitImpl(_findForkPoint(currentBranch, branchToMerge)), conflictingFiles);
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IMergeDiff> merge(@NotNull IBranch parentBranch, @NotNull IBranch branchToMerge) throws Exception {
        String parentID = parentBranch.getId();
        String toMergeID = branchToMerge.getId();
        List<IMergeDiff> mergeConflicts = new ArrayList<>();
        if (status.blockingFirst().getConflicting().size() > 0) {
            RevCommit forkCommit = _findForkPoint(parentID, toMergeID);
            mergeConflicts = _getMergeConflicts(parentID, toMergeID, new CommitImpl(forkCommit), status.blockingFirst().getConflicting());
        }
        try {
            checkout(parentID);
        } catch (Exception e) {
            throw new Exception("Unable to checkout the parentBranch: " + parentID + " at the merge command", e);
        }
        ObjectId mergeBase;
        try {
            mergeBase = git.getRepository().resolve(toMergeID);
        } catch (IOException e) {
            throw new Exception("Unable to merge the branch " + toMergeID + " and " + parentID, e);
        }
        try {
            MergeResult mergeResult = git.merge()
                    .include(mergeBase)
                    .setCommit(false)
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF).call();
            if (mergeResult.getConflicts() != null) {
                RevCommit forkCommit = _findForkPoint(parentID, toMergeID);
                if (forkCommit != null)
                    mergeConflicts = _getMergeConflicts(parentID, toMergeID, new CommitImpl(forkCommit), mergeResult.getConflicts().keySet());
            }
        } catch (GitAPIException e) {
            throw new Exception("Unable to execute the merge command: " + parentID + "and " + toMergeID, e);
        }
        return mergeConflicts;
    }

    /**
     * @param conflictingFile one of the Files with a conflict that were modified by git/JGit
     * @return id of the branch that gets merged ("THEIRS" for merges)
     */
    private String _getConflictingBranch(File conflictingFile) throws IOException {
        return Files.lines(conflictingFile.toPath()).filter(line -> line.startsWith(">>>>>>>>")).findFirst().map(s -> s.replace(">", "").trim()).orElse(null);
    }

    /**
     * @param currentBranch Identifier for the current branch
     * @param branchToMerge Identifier for the branch that should be merged into the current one
     * @param forkCommit    the commit where the branches of the two commits diverged
     * @param conflicts     Set of Strings (filepaths) that give the files with conflicts that occurred during the merge
     * @return List<IMergeDiff> describing the changes from the fork commit to each branch
     * @throws Exception if JGit encountered an error condition
     */
    private List<IMergeDiff> _getMergeConflicts(String currentBranch, String branchToMerge, CommitImpl forkCommit, Set<String> conflicts) throws Exception {
        List<IMergeDiff> mergeConflicts = new ArrayList<>();
        ICommit parentBranchCommit;
        ICommit toMergeCommit;
        try {
            parentBranchCommit = new CommitImpl(git.getRepository().parseCommit(git.getRepository().resolve(currentBranch)));
            toMergeCommit = new CommitImpl(git.getRepository().parseCommit(git.getRepository().resolve(branchToMerge)));
        } catch (IOException e) {
            throw new Exception(e);
        }
        List<IFileDiff> parentDiffList = diff(parentBranchCommit, forkCommit);
        List<IFileDiff> toMergeDiffList = diff(toMergeCommit, forkCommit);
        for (IFileDiff parentDiff : parentDiffList) {
            if (conflicts.contains(parentDiff.getFilePath(EChangeSide.NEW))) {
                for (IFileDiff toMergeDiff : toMergeDiffList) {
                    if (toMergeDiff.getFilePath(EChangeSide.NEW).equals(parentDiff.getFilePath(EChangeSide.NEW))) {
                        mergeConflicts.add(new MergeDiffImpl(parentDiff, toMergeDiff));
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
    private RevCommit _findForkPoint(String parentBranchName, String foreignBranchName) throws IOException {
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
    public List<String> getCommitedFiles(String commitId) throws Exception {
        RevCommit thisCommit = _getRevCommit(commitId);
        List<DiffEntry> diffEntries = new ArrayList<>();
        for (RevCommit parent : thisCommit.getParents()) {
            diffEntries.addAll(_doDiff(thisCommit.getId(), parent.getId()));
        }
        return diffEntries.stream()
                .map(DiffEntry::getOldPath)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommit getCommit(@NotNull String identifier) throws Exception {
        return new CommitImpl(_getRevCommit(identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICommit> getCommits(IBranch sourceBranch) throws Exception {
        List<ICommit> commitList = new ArrayList<>();
        Iterable<RevCommit> refCommits;
        LogCommand logCommand = git.log().add(git.getRepository().resolve(sourceBranch.getName()));
        logCommand.setMaxCount(300);
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

    /**
     * {@inheritDoc}
     */
    @NotNull
    public List<CommitHistoryTreeListItem> getCommitHistoryTreeList(@NotNull List<ICommit> commits) {
        List<CommitHistoryTreeListItem> commitHistoryTreeList = new ArrayList<>();
        if (!commits.isEmpty()) {
            // special case for first item
            List<AncestryLine> ancestryLines = new ArrayList<>();
            Color firstColor = colorRoulette.get();
            ancestryLines.add(new AncestryLine(commits.get(0), firstColor == null ? Color.green : firstColor, colorRoulette));
            commitHistoryTreeList.add(new CommitHistoryTreeListItem(commits.get(0), ancestryLines, colorRoulette));
            // main loop iterating over the commits
            for (int index = 1; index < commits.size() - 1; index++) {
                commitHistoryTreeList.add(commitHistoryTreeList.get(commitHistoryTreeList.size() - 1).nextItem(commits.get(index), commits.get(index + 1)));
            }
            // special case for the last item in the list, only needed if more than one item in the list
            if (commits.size() > 1) {
                commitHistoryTreeList.add(commitHistoryTreeList.get(commitHistoryTreeList.size() - 1).nextItem(commits.get(commits.size() - 1), null));
            }
        }
        colorRoulette.reset();
        return commitHistoryTreeList;
    }

    @Override
    public String getDirectory() {

        return String.valueOf(git.getRepository().getDirectory());
    }

    @Nullable
    @Override
    public File getTopLevelDirectory() {
        return git.getRepository().getDirectory().getParent() != null ? new File(git.getRepository().getDirectory().getParent()) : null;
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

        Ref branch = git.getRepository().getRefDatabase().getRef(branchString);

        List<Ref> list = refList.stream()
                .filter(pBranch -> pBranch.getName()
                        .equals(branch.getName()))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            throw new Exception("There is no element in the branch list");
        }
        return new BranchImpl(list.get(0));
    }

    @Override
    public Observable<IBranch> getCurrentBranch(){
        return currentBranch;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Observable<List<IBranch>> getBranches() {
        return branchList;
    }

    /**
     * @param identifier Id of the commit to be retrieved
     * @return RevCommit that matches the passed identifier
     * @throws IOException if JGit encountered an error condition
     */
    private RevCommit _getRevCommit(String identifier) throws IOException {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            ObjectId commitId = ObjectId.fromString(identifier);
            return revWalk.parseCommit(commitId);
        }
    }

    /**
     * @param currentId   Id of the current branch/commit/file
     * @param compareToId Id of the branch/commit/file to be compared with the current one
     * @return List<DiffEntry> with the DiffEntrys that make the difference between the two commits/branches/files
     * @throws Exception if JGit encountered an error condition
     */
    private List<DiffEntry> _doDiff(ObjectId currentId, ObjectId compareToId) throws Exception {
        CanonicalTreeParser oldTreeIter = prepareTreeParser(git.getRepository(), compareToId);
        CanonicalTreeParser newTreeIter = prepareTreeParser(git.getRepository(), currentId);

        try {
            return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
        } catch (GitAPIException e) {
            throw new Exception("Unable to show changes between commits", e);
        }
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
        return new FileStatusImpl(currentStatus, git.getRepository().getDirectory());
    }

    private IBranch _currentBranch() {
        try {
            String branch = git.getRepository().getFullBranch();
            return getBranch(branch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            IFileSystemChangeListener listener = () -> pOnNext.accept(_status());
            pListenableValue.addListener(listener);
            return listener;
        }

        @Override
        protected void removeListener(@NotNull IFileSystemObserver pListenableValue, @NotNull IFileSystemChangeListener pLISTENER) {
            pListenableValue.removeListener(pLISTENER);
        }
    }
}