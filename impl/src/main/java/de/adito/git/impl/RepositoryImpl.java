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
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.*;
import org.jetbrains.annotations.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.adito.git.impl.Util.getRelativePath;

/**
 * @author A.Arnold 21.09.2018
 */
public class RepositoryImpl implements IRepository
{

  private Git git;
  private final Observable<Optional<List<IBranch>>> branchList;
  private final Observable<IFileStatus> status;
  private final BehaviorSubject<Optional<IBranch>> currentBranchObservable;
  private final ColorRoulette colorRoulette;

  @Inject
  public RepositoryImpl(IFileSystemObserverProvider pFileSystemObserverProvider, ColorRoulette pColorRoulette,
                        @Assisted IRepositoryDescription pRepositoryDescription) throws IOException
  {
    colorRoulette = pColorRoulette;
    git = new Git(GitRepositoryProvider.get(pRepositoryDescription));
    branchList = BehaviorSubject.createDefault(Optional.of(_branchList()));

    // listen for changes in the fileSystem for the status command
    status = Observable.create(new _FileSystemChangeObservable(pFileSystemObserverProvider.getFileSystemObserver(pRepositoryDescription)))
        .subscribeWith(BehaviorSubject.createDefault(_status()));

    // Current Branch
    IBranch curBranch = _currentBranch();
    currentBranchObservable = BehaviorSubject.createDefault(curBranch == null ? Optional.empty() : Optional.of(curBranch));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(List<File> pAddList) throws AditoGitException
  {
    AddCommand adder = git.add();
    for (File file : pAddList)
    {
      adder.addFilepattern(getRelativePath(file, git));
    }
    try
    {
      adder.call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to add Files to staging area", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String commit(@NotNull String pMessage) throws AditoGitException
  {
    CommitCommand commit = git.commit();
    RevCommit revCommit;
    try
    {
      revCommit = commit.setMessage(pMessage).call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to commit to local Area", e);
    }
    if (revCommit == null)
    {
      return "";
    }
    return ObjectId.toString(revCommit.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String commit(@NotNull String pMessage, List<File> pFileList, boolean pIsAmend) throws AditoGitException
  {
    CommitCommand commit = git.commit();
    RevCommit revCommit;
    for (File file : pFileList)
    {
      commit.setOnly(getRelativePath(file, git));
    }
    try
    {
      revCommit = commit.setMessage(pMessage).setAmend(pIsAmend).call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to commit to local Area", e);
    }
    if (revCommit == null)
    {
      return "";
    }
    return ObjectId.toString(revCommit.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean push()
  {
    PushCommand push = git.push()
        .setTransportConfigCallback(new TransportConfigCallbackImpl(null, null));
    try
    {
      push.call();
    }
    catch (JGitInternalException | GitAPIException e)
    {
      throw new IllegalStateException("Unable to push into remote Git repository", e);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IRebaseResult pull(boolean pDoAbort) throws AditoGitException
  {
    try
    {
      if (pDoAbort)
      {
        RebaseCommand rebaseCommand = git.rebase();
        rebaseCommand.setOperation(RebaseCommand.Operation.ABORT);
        RebaseResult rebaseResult = rebaseCommand.call();
        if (rebaseResult.getStatus() == RebaseResult.Status.ABORTED)
          return new RebaseResultImpl(Collections.emptyList(), rebaseResult.getStatus());
        else
          throw new AditoGitException("could not abort rebase");
      }
      IRebaseResult iRebaseResult;
      if (!git.getRepository().getRepositoryState().isRebasing())
      {
        String currentHeadName = git.getRepository().getFullBranch();
        String targetName = new BranchConfig(git.getRepository().getConfig(), git.getRepository().getBranch()).getRemoteTrackingBranch();
        PullCommand pullCommand = git.pull();
        pullCommand.setRebase(true);
        try
        {
          PullResult pullResult = pullCommand.call();
          iRebaseResult = _handlePullResult(pullResult::getRebaseResult, ObjectId.toString(pullResult.getRebaseResult().getCurrentCommit().getId()),
                                            targetName, new CommitImpl(_findForkPoint(currentHeadName, targetName)));
        }
        catch (GitAPIException e)
        {
          throw new AditoGitException("Unable to pull new files", e);
        }
      }
      else
      {
        IFileStatus currentStatus = status.blockingFirst();
        String targetName;
        String currentHeadName;
        try (BufferedReader reader = new BufferedReader(new FileReader(
            new File(git.getRepository().getDirectory().getAbsolutePath(), "rebase-merge/head"))))
        {
          currentHeadName = reader.readLine();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(
            new File(git.getRepository().getDirectory().getAbsolutePath(), "rebase-merge/onto"))))
        {
          targetName = reader.readLine();
        }
        if (!currentStatus.getConflicting().isEmpty())
        {
          RevCommit forkCommit = _findForkPoint(targetName, currentHeadName);
          return new RebaseResultImpl(_getMergeConflicts(targetName, currentHeadName, new CommitImpl(forkCommit),
                                                         currentStatus.getConflicting()), RebaseResult.Status.CONFLICTS);
        }
        RebaseCommand rebaseCommand = git.rebase();
        rebaseCommand.setOperation(RebaseCommand.Operation.CONTINUE);
        RebaseResult rebaseResult = rebaseCommand.call();
        iRebaseResult = _handlePullResult(() -> rebaseResult, currentHeadName, targetName,
                                          rebaseResult.getCurrentCommit() == null ? null
                                              : new CommitImpl(rebaseResult.getCurrentCommit().getParent(0)));
      }
      return iRebaseResult;
    }
    catch (IOException | GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  private IRebaseResult _handlePullResult(Supplier<RebaseResult> pResultSupplier,
                                          String pCurrHeadName, String pTargetName, CommitImpl pForkPoint) throws AditoGitException
  {
    if (!pResultSupplier.get().getStatus().isSuccessful())
    {
      Set<String> conflictFilesSet;
      List<String> conflictFiles = pResultSupplier.get().getConflicts();
      if (conflictFiles == null)
      {
        IFileStatus currentStatus = _status();
        conflictFilesSet = currentStatus.getConflicting();
      }
      else
      {
        conflictFilesSet = Set.copyOf(conflictFiles);
      }
      if (!conflictFilesSet.isEmpty())
      {
        return new RebaseResultImpl(_getMergeConflicts(pCurrHeadName, pTargetName, pForkPoint, conflictFilesSet),
                                    pResultSupplier.get().getStatus());
      }
    }
    return new RebaseResultImpl(Collections.emptyList(), pResultSupplier.get().getStatus());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fetch() throws AditoGitException
  {
    fetch(true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fetch(boolean pPrune) throws AditoGitException
  {
    try
    {
      git.fetch().setRemoveDeletedRefs(pPrune).call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @Override
  public List<IFileChangeChunk> diff(@NotNull String pFileContents, File pCompareWith) throws IOException
  {
    String headId = ObjectId.toString(git.getRepository().resolve(Constants.HEAD));
    RawText headFileContents = new RawText(getFileContents(getFileVersion(headId, Util.getRelativePath(pCompareWith, git))).getBytes());
    RawText currentFileContents = new RawText(pFileContents.getBytes());
    EditList linesChanged = new HistogramDiff().diff(RawTextComparator.WS_IGNORE_TRAILING, headFileContents, currentFileContents);
    List<IFileChangeChunk> changeChunks = new ArrayList<>();
    for (Edit edit : linesChanged)
    {
      changeChunks.add(new FileChangeChunkImpl(edit, "", "", EnumMappings._toEChangeType(edit.getType())));
    }
    return changeChunks;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull List<IFileDiff> diff(@NotNull ICommit pOriginal, @NotNull ICommit pCompareTo) throws AditoGitException
  {
    try
    {
      List<IFileDiff> listDiffImpl = new ArrayList<>();

      List<DiffEntry> listDiff = _doDiff(ObjectId.fromString(pOriginal.getId()), ObjectId.fromString(pCompareTo.getId()));

      if (listDiff != null)
      {
        for (DiffEntry diff : listDiff)
        {
          try (DiffFormatter formatter = new DiffFormatter(null))
          {
            formatter.setRepository(git.getRepository());
            FileHeader fileHeader = formatter.toFileHeader(diff);
            listDiffImpl.add(new FileDiffImpl(
                diff,
                fileHeader,
                getFileContents(getFileVersion(pCompareTo.getId(), diff.getOldPath())),
                getFileContents(getFileVersion(pOriginal.getId(), diff.getNewPath()))));
          }
        }
      }
      return listDiffImpl;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull List<IFileDiff> diff(@Nullable List<File> pFilesToDiff, @Nullable ICommit pCompareWith) throws AditoGitException
  {
    try
    {
      List<IFileDiff> returnList = new ArrayList<>();

      // prepare the TreeIterators for the local working copy and the files in HEAD
      FileTreeIterator fileTreeIterator = new FileTreeIterator(git.getRepository());
      ObjectId compareWithId = git.getRepository().resolve(pCompareWith == null ? Constants.HEAD : pCompareWith.getId());
      CanonicalTreeParser treeParser = _prepareTreeParser(git.getRepository(), compareWithId);

      // Use the DiffFormatter to retrieve a list of changes
      DiffFormatter diffFormatter = new DiffFormatter(null);
      diffFormatter.setRepository(git.getRepository());
      diffFormatter.setDiffComparator(RawTextComparator.WS_IGNORE_TRAILING);
      List<TreeFilter> pathFilters = new ArrayList<>();
      if (pFilesToDiff != null)
      {
        for (File fileToDiff : pFilesToDiff)
        {
          pathFilters.add(PathFilter.create(Util.getRelativePath(fileToDiff, git)));
        }
        if (pathFilters.size() > 1)
        {
          diffFormatter.setPathFilter(OrTreeFilter.create(pathFilters));
        }
        else if (!pathFilters.isEmpty())
        {
          diffFormatter.setPathFilter(pathFilters.get(0));
        }
      }
      List<DiffEntry> diffList = diffFormatter.scan(treeParser, fileTreeIterator);

      for (DiffEntry diffEntry : diffList)
      {
        // check if the diff is of a file in  the passed list, except if filesToDiff is null (all files are valid).
        if (pFilesToDiff == null
            || pFilesToDiff.stream().anyMatch(file -> getRelativePath(file, git).equals(diffEntry.getNewPath()))
            || pFilesToDiff.stream().anyMatch(file -> getRelativePath(file, git).equals(diffEntry.getOldPath())))
        {
          FileHeader fileHeader = diffFormatter.toFileHeader(diffEntry);
          // Can't use the ObjectLoader or anything similar provided by JGit because it wouldn't find the blob, so parse file by hand
          StringBuilder newFileLines = new StringBuilder();
          if (!"/dev/null".equals(diffEntry.getNewPath()))
          {
            try (Stream<String> lines = Files.lines(new File(getTopLevelDirectory(), diffEntry.getNewPath()).toPath()))
            {
              lines.forEach(line -> newFileLines.append(line).append("\n"));
            }
          }
          String oldFileContents = "/dev/null".equals(diffEntry.getOldPath()) ? "" :
              getFileContents(getFileVersion(ObjectId.toString(compareWithId), diffEntry.getOldPath()));
          returnList.add(new FileDiffImpl(diffEntry, fileHeader,
                                          oldFileContents, newFileLines.toString()));
        }
      }
      return returnList;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFileContents(String pIdentifier) throws IOException
  {
    ObjectLoader loader = git.getRepository().open(ObjectId.fromString(pIdentifier));
    return new String(loader.getBytes(), StandardCharsets.UTF_8);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFileVersion(String pCommitId, String pFilename) throws IOException
  {
    try (RevWalk revWalk = new RevWalk(git.getRepository()))
    {
      RevCommit commit = revWalk.parseCommit(ObjectId.fromString(pCommitId));
      RevTree tree = commit.getTree();

      // find the specific file
      try (TreeWalk treeWalk = new TreeWalk(git.getRepository()))
      {
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(pFilename));
        if (!treeWalk.next())
        {
          throw new IllegalStateException("Could not find file " + pFilename);
        }
        return ObjectId.toString(treeWalk.getObjectId(0));
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean clone(@NotNull String pUrl, @NotNull File pLocalPath)
  {

    if (Util.isDirEmpty(pLocalPath))
    {
      CloneCommand cloneRepo = Git.cloneRepository()
          // TODO: 25.09.2018 NetBeans pPassword
          .setTransportConfigCallback(new TransportConfigCallbackImpl(null, null))
          .setURI(pUrl)
          .setDirectory(new File(pLocalPath, ""));
      try
      {
        cloneRepo.call();
      }
      catch (GitAPIException e)
      {
        throw new RuntimeException(e);
      }
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Observable<IFileStatus> getStatus()
  {
    return status;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void ignore(@NotNull List<File> pFiles) throws IOException
  {
    File gitIgnore = new File(GitRepositoryProvider.get().getDirectory().getParent(), ".gitignore");
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true)))
    {
      for (File file : pFiles)
      {
        outputStream.write((getRelativePath(file, git) + "\n").getBytes());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void exclude(@NotNull List<File> pFiles) throws IOException
  {
    File gitIgnore = new File(GitRepositoryProvider.get().getDirectory(), "info/exclude");
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true)))
    {
      for (File file : pFiles)
      {
        outputStream.write((getRelativePath(file, git) + "\n").getBytes());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void revertWorkDir(@NotNull List<File> pFiles) throws AditoGitException
  {
    try
    {
      CheckoutCommand checkoutCommand = git.checkout();
      for (File file : pFiles)
      {
        checkoutCommand.addPath(Util.getRelativePath(file, git));
      }
      checkoutCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(@NotNull List<File> pFiles) throws AditoGitException
  {
    try
    {
      ResetCommand resetCommand = git.reset();
      for (File file : pFiles)
        resetCommand.addPath(Util.getRelativePath(file, git));
      resetCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(@NotNull String pIdentifier, @NotNull EResetType pResetType) throws AditoGitException
  {
    try
    {
      ResetCommand resetCommand = git.reset();
      resetCommand.setRef(pIdentifier);
      if (pResetType == EResetType.HARD)
        resetCommand.setMode(ResetCommand.ResetType.HARD);
      else if (pResetType == EResetType.MIXED)
        resetCommand.setMode(ResetCommand.ResetType.MIXED);
      else resetCommand.setMode(ResetCommand.ResetType.SOFT);
      resetCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createBranch(@NotNull String pBranchName, boolean pCheckout) throws AditoGitException
  {
    try
    {
      List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

      if (refs.stream().anyMatch(ref -> ref.getName().equals("refs/heads/" + pBranchName)))
      {
        throw new AditoGitException("Branch already exist. " + pBranchName);
      }

      git.branchCreate().setName(pBranchName).call();
      // the next line of code is for an automatically push after creating a branch
      if (pCheckout)
      {
        checkout(pBranchName);
      }
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to create new branch: " + pBranchName, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteBranch(@NotNull String pBranchName) throws AditoGitException
  {
    String destination = "refs/heads/" + pBranchName;
    try
    {
      git.branchDelete()
          .setBranchNames(destination)
          .call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to delete the branch: " + pBranchName, e);
    }
    RefSpec refSpec = new RefSpec().setSource(null).setDestination(destination);
    try
    {
      git.push().setRefSpecs(refSpec).setRemote("origin").call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to push the delete branch comment @ " + pBranchName, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkout(@NotNull String pBranchName) throws AditoGitException
  {
    checkout(getBranch(pBranchName));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkout(@NotNull IBranch pBranch) throws AditoGitException
  {
    CheckoutCommand checkout = git.checkout().setName(pBranch.getName()).setCreateBranch(false).setStartPoint(pBranch.getName());
    try
    {
      checkout.call();
      currentBranchObservable.onNext(Optional.of(pBranch));
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to checkout Branch " + pBranch.getName(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<IMergeDiff> getMergeConflicts() throws AditoGitException
  {
    try
    {
      Set<String> conflictingFiles = status.blockingFirst().getConflicting();
      if (!conflictingFiles.isEmpty())
      {
        File aConflictingFile = new File(git.getRepository().getDirectory().getParent(), conflictingFiles.iterator().next());
        String currentBranch = git.getRepository().getBranch();
        String branchToMerge = _getConflictingBranch(aConflictingFile);
        return _getMergeConflicts(currentBranch, branchToMerge, new CommitImpl(_findForkPoint(currentBranch, branchToMerge)), conflictingFiles);
      }
      return Collections.emptyList();
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IMergeDiff> merge(@NotNull IBranch pParentBranch, @NotNull IBranch pBranchToMerge) throws AditoGitException
  {
    try
    {
      String parentID = pParentBranch.getId();
      String toMergeID = pBranchToMerge.getId();
      List<IMergeDiff> mergeConflicts = new ArrayList<>();
      if (!status.blockingFirst().getConflicting().isEmpty())
      {
        RevCommit forkCommit = _findForkPoint(parentID, toMergeID);
        return _getMergeConflicts(parentID, toMergeID, new CommitImpl(forkCommit), status.blockingFirst().getConflicting());
      }
      try
      {
        checkout(parentID);
      }
      catch (Exception e)
      {
        throw new AditoGitException("Unable to checkout the parentBranch: " + parentID + " at the merge command", e);
      }
      ObjectId mergeBase;
      try
      {
        mergeBase = git.getRepository().resolve(toMergeID);
      }
      catch (IOException e)
      {
        throw new AditoGitException("Unable to merge the branch " + toMergeID + " and " + parentID, e);
      }
      try
      {
        MergeResult mergeResult = git.merge()
            .include(mergeBase)
            .setCommit(false)
            .setFastForward(MergeCommand.FastForwardMode.NO_FF).call();
        if (mergeResult.getConflicts() != null)
        {
          RevCommit forkCommit = _findForkPoint(parentID, toMergeID);
          if (forkCommit != null)
            mergeConflicts = _getMergeConflicts(parentID, toMergeID, new CommitImpl(forkCommit), mergeResult.getConflicts().keySet());
        }
      }
      catch (GitAPIException e)
      {
        throw new AditoGitException("Unable to execute the merge command: " + parentID + "and " + toMergeID, e);
      }
      return mergeConflicts;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * @param pConflictingFile one of the Files with a conflict that were modified by git/JGit
   * @return id of the branch that gets merged ("THEIRS" for merges)
   */
  private String _getConflictingBranch(File pConflictingFile) throws IOException
  {
    try (Stream<String> lines = Files.lines(pConflictingFile.toPath()))
    {
      return lines.filter(line -> line.startsWith(">>>>>>>>"))
          .findFirst()
          .map(s -> s.replace(">", "").trim())
          .orElse(null);
    }
  }

  /**
   * @param pCurrentBranch Identifier for the current branch
   * @param pBranchToMerge Identifier for the branch that should be merged into the current one
   * @param pForkCommit    the commit where the branches of the two commits diverged
   * @param pConflicts     Set of Strings (filepaths) that give the files with conflicts that occurred during the merge
   * @return List<IMergeDiff> describing the changes from the fork commit to each branch
   * @throws AditoGitException if JGit encountered an error condition
   */
  private List<IMergeDiff> _getMergeConflicts(String pCurrentBranch, String pBranchToMerge,
                                              ICommit pForkCommit, Set<String> pConflicts) throws AditoGitException
  {
    List<IMergeDiff> mergeConflicts = new ArrayList<>();
    ICommit parentBranchCommit;
    ICommit toMergeCommit;
    try
    {
      parentBranchCommit = new CommitImpl(git.getRepository().parseCommit(git.getRepository().resolve(pCurrentBranch)));
      toMergeCommit = new CommitImpl(git.getRepository().parseCommit(git.getRepository().resolve(pBranchToMerge)));
    }
    catch (IOException e)
    {
      throw new AditoGitException(e);
    }
    List<IFileDiff> parentDiffList = diff(parentBranchCommit, pForkCommit);
    List<IFileDiff> toMergeDiffList = diff(toMergeCommit, pForkCommit);
    for (IFileDiff parentDiff : parentDiffList)
    {
      if (pConflicts.contains(parentDiff.getFilePath(EChangeSide.NEW)))
      {
        for (IFileDiff toMergeDiff : toMergeDiffList)
        {
          if (toMergeDiff.getFilePath(EChangeSide.NEW).equals(parentDiff.getFilePath(EChangeSide.NEW)))
          {
            mergeConflicts.add(new MergeDiffImpl(parentDiff, toMergeDiff));
          }
        }
      }
    }
    return mergeConflicts;
  }

  /**
   * @param pParentBranchName  name (not id) of the branch currently on
   * @param pForeignBranchName name (not id) of the branch (or id for a commit) for which to find the closest forkPoint to this branch
   * @return RevCommit that is the forkPoint between the two branches, or null if not available
   * @throws IOException if an error occurs during parsing
   */
  @Nullable
  private RevCommit _findForkPoint(String pParentBranchName, String pForeignBranchName) throws IOException
  {
    try (RevWalk walk = new RevWalk(git.getRepository()))
    {
      RevCommit foreignCommit = walk.lookupCommit(git.getRepository().resolve(pForeignBranchName));
      LinkedList<ObjectId> parentsToParse = new LinkedList<>();
      parentsToParse.add(git.getRepository().resolve(pParentBranchName));
      while (!parentsToParse.isEmpty())
      {
        RevCommit commit = walk.lookupCommit(parentsToParse.poll());
        // check if foreignCommit is reachable from the currently selected commit
        if (walk.isMergedInto(commit, foreignCommit))
        {
          // check if commit is a valid commit that does not contain errors when parsed
          walk.parseBody(commit);
          return commit;
        }
        else
        {
          parentsToParse.addAll(Arrays.stream(commit.getParents()).map(RevObject::getId).collect(Collectors.toList()));
        }
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IFileChangeType> getCommittedFiles(String pCommitId) throws AditoGitException
  {
    try
    {
      RevCommit thisCommit = _getRevCommit(pCommitId);
      List<DiffEntry> diffEntries = new ArrayList<>();
      for (RevCommit parent : thisCommit.getParents())
      {
        diffEntries.addAll(_doDiff(thisCommit.getId(), parent.getId()));
      }
      return diffEntries.stream()
          .map(pDiffEntry -> new FileChangeTypeImpl(
              new File(pDiffEntry.getOldPath()), EnumMappings._toEChangeType(pDiffEntry.getChangeType())))
          .distinct()
          .collect(Collectors.toList());
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ICommit getCommit(@Nullable String pIdentifier) throws AditoGitException
  {
    try
    {
      if (pIdentifier == null)
      {
        // only one RevCommit expected as result, so only take the first RevCommit
        return new CommitImpl(git.log().setMaxCount(1).call().iterator().next());
      }
      return new CommitImpl(_getRevCommit(pIdentifier));
    }
    catch (IOException | GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ICommit> getCommits(@Nullable IBranch pSourceBranch) throws AditoGitException
  {
    return getCommits(pSourceBranch, -1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ICommit> getCommits(@Nullable IBranch pSourceBranch, int pNumCommits) throws AditoGitException
  {
    return getCommits(pSourceBranch, -1, pNumCommits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ICommit> getCommits(@Nullable IBranch pSourceBranch, int pIndexFrom, int pNumCommits) throws AditoGitException
  {
    return _getCommits(pSourceBranch, null, pIndexFrom, pNumCommits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ICommit> getCommits(@Nullable File pForFile) throws AditoGitException
  {
    return getCommits(pForFile, -1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ICommit> getCommits(@Nullable File pFile, int pNumCommits) throws AditoGitException
  {
    return getCommits(pFile, -1, pNumCommits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ICommit> getCommits(@Nullable File pFile, int pIndexFrom, int pNumCommits) throws AditoGitException
  {
    return _getCommits(null, pFile, pIndexFrom, pNumCommits);
  }

  /**
   * @param pSourceBranch IBranch that the retrieved commits should be from
   * @param pFile         File that is affected by all commits that are retrieved
   * @param pIndexFrom    number of (matching) commits to skip before retrieving commits from the log
   * @param pNumCommits   number of (matching) commits to retrieve
   * @return List of ICommits matching the provided criteria
   * @throws AditoGitException if JGit throws an exception/returns null
   */
  private List<ICommit> _getCommits(@Nullable IBranch pSourceBranch, @Nullable File pFile, int pIndexFrom, int pNumCommits) throws AditoGitException
  {
    try
    {
      List<ICommit> commitList = new ArrayList<>();
      Iterable<RevCommit> refCommits;
      LogCommand logCommand = git.log();
      if (pSourceBranch != null)
      {
        logCommand.add(git.getRepository().resolve(pSourceBranch.getName()));
      }
      else
      {
        logCommand.all();
      }
      if (pFile != null)
      {
        logCommand.addPath(getRelativePath(pFile, git));
      }
      if (pIndexFrom >= 0)
      {
        logCommand.setSkip(pIndexFrom);
      }
      if (pNumCommits >= 0)
      {
        logCommand.setMaxCount(pNumCommits);
      }
      try
      {
        refCommits = logCommand.call();
      }
      catch (GitAPIException e)
      {
        throw new AditoGitException("Unable to check the commits of one branch and/or file. Branch: " + pSourceBranch + ", File: " + pFile, e);
      }
      if (refCommits != null)
      {
        refCommits.forEach(commit -> commitList.add(new CommitImpl(commit)));
      }
      else
      {
        throw new AditoGitException("Object returned by JGit was null while trying to retrieve commits. Branch: " + pSourceBranch + ", File: " + pFile);
      }
      return commitList;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public List<CommitHistoryTreeListItem> getCommitHistoryTreeList(@NotNull List<ICommit> pCommits, @Nullable CommitHistoryTreeListItem pStartCHTLI)
  {
    List<CommitHistoryTreeListItem> commitHistoryTreeList = new ArrayList<>();
    if (!pCommits.isEmpty())
    {
      // special case for first item
      if (pStartCHTLI != null)
      {
        commitHistoryTreeList.add(pStartCHTLI.nextItem(pCommits.get(0), pCommits.size() > 1 ? pCommits.get(1) : null));
      }
      else
      {
        List<AncestryLine> ancestryLines = new ArrayList<>();
        ancestryLines.add(new AncestryLine(pCommits.get(0), colorRoulette.get(), colorRoulette, true));
        commitHistoryTreeList.add(new CommitHistoryTreeListItem(pCommits.get(0), ancestryLines, colorRoulette));
      }
      // main loop iterating over the commits
      for (int index = 1; index < pCommits.size() - 1; index++)
      {
        commitHistoryTreeList.add(commitHistoryTreeList.get(commitHistoryTreeList.size() - 1).nextItem(pCommits.get(index), pCommits.get(index + 1)));
      }
      // special case for the last item in the list, only needed if more than one item in the list
      if (pCommits.size() > 1)
      {
        commitHistoryTreeList.add(commitHistoryTreeList.get(commitHistoryTreeList.size() - 1).nextItem(pCommits.get(pCommits.size() - 1), null));
      }
    }
    colorRoulette.reset();
    return commitHistoryTreeList;
  }

  @Override
  public String getDirectory()
  {

    return String.valueOf(git.getRepository().getDirectory());
  }

  @Nullable
  @Override
  public File getTopLevelDirectory()
  {
    return git.getRepository().getDirectory().getParent() != null ? new File(git.getRepository().getDirectory().getParent()) : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IBranch getBranch(@NotNull String pBranchString) throws AditoGitException
  {
    try
    {
      return new BranchImpl(git.getRepository().getRefDatabase().getRef(pBranchString));
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @Override
  public Observable<Optional<IBranch>> getCurrentBranch()
  {
    return currentBranchObservable;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public Observable<Optional<List<IBranch>>> getBranches()
  {
    return branchList;
  }

  /**
   * @param pIdentifier Id of the commit to be retrieved
   * @return RevCommit that matches the passed identifier
   * @throws IOException if JGit encountered an error condition
   */
  private RevCommit _getRevCommit(String pIdentifier) throws IOException
  {
    try (RevWalk revWalk = new RevWalk(git.getRepository()))
    {
      ObjectId commitId = ObjectId.fromString(pIdentifier);
      return revWalk.parseCommit(commitId);
    }
  }

  /**
   * @param pCurrentId   Id of the current branch/commit/file
   * @param pCompareToId Id of the branch/commit/file to be compared with the current one
   * @return List<DiffEntry> with the DiffEntrys that make the difference between the two commits/branches/files
   * @throws AditoGitException if JGit encountered an error condition
   */
  private List<DiffEntry> _doDiff(ObjectId pCurrentId, ObjectId pCompareToId) throws AditoGitException
  {
    try
    {
      CanonicalTreeParser oldTreeIter = _prepareTreeParser(git.getRepository(), pCompareToId);
      CanonicalTreeParser newTreeIter = _prepareTreeParser(git.getRepository(), pCurrentId);

      try
      {
        return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
      }
      catch (GitAPIException e)
      {
        throw new AditoGitException("Unable to show changes between commits", e);
      }
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  private List<IBranch> _branchList()
  {
    ListBranchCommand listBranchCommand = git.branchList().setListMode(ListBranchCommand.ListMode.ALL);
    List<IBranch> branches = new ArrayList<>();
    List<Ref> refBranchList;
    try
    {
      refBranchList = listBranchCommand.call();
    }
    catch (GitAPIException e)
    {
      throw new RuntimeException(e);
    }
    if (refBranchList != null)
    {
      refBranchList.forEach(branch -> branches.add(new BranchImpl(branch)));
    }
    return branches;
  }

  private IFileStatus _status()
  {
    StatusCommand statusCommand = git.status();
    Status currentStatus;
    try
    {
      currentStatus = statusCommand.call();
    }
    catch (GitAPIException e)
    {
      throw new RuntimeException(e);
    }
    return new FileStatusImpl(currentStatus, git.getRepository().getDirectory());
  }

  private IBranch _currentBranch()
  {
    try
    {
      String branch = git.getRepository().getFullBranch();
      if (git.getRepository().getRefDatabase().getRef(branch) == null)
      {
        return new BranchImpl(git.getRepository().resolve(branch));
      }
      return getBranch(branch);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Ref isn't a Branch or a Commit to checkout");
    }
  }

  /**
   * Helperfunction to prepare the TreeParser for the diff function
   *
   * @param pRepository the (git) repository
   * @param pObjectId   the objectId for the commit/Branch that the Tree should be prepared for
   * @return initialised CanonicalTreeParser
   * @throws IOException if an error occurs, such as an invalid ID or the treeParser cannot be reset
   */
  private CanonicalTreeParser _prepareTreeParser(Repository pRepository, ObjectId pObjectId) throws IOException
  {
    try (RevWalk walk = new RevWalk(pRepository))
    {
      RevCommit commit = walk.parseCommit(pObjectId);
      RevTree tree = walk.parseTree(commit.getTree().getId());

      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      try (ObjectReader reader = pRepository.newObjectReader())
      {
        treeParser.reset(reader, tree.getId());
      }

      walk.dispose();

      return treeParser;
    }
  }

  /**
   * Bridge from the FileSystemChangeListener to Observables
   */
  private class _FileSystemChangeObservable extends AbstractListenerObservable<IFileSystemChangeListener, IFileSystemObserver, IFileStatus>
  {

    _FileSystemChangeObservable(@NotNull IFileSystemObserver pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected IFileSystemChangeListener registerListener(@NotNull IFileSystemObserver pListenableValue, @NotNull Consumer<IFileStatus> pOnNext)
    {
      IFileSystemChangeListener listener = () -> pOnNext.accept(_status());
      pListenableValue.addListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull IFileSystemObserver pListenableValue, @NotNull IFileSystemChangeListener pLISTENER)
    {
      pListenableValue.removeListener(pLISTENER);
    }
  }
}