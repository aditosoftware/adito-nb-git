package de.adito.git.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import de.adito.git.api.TrackedBranchStatusCache;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.impl.dag.DAGFilterIterator;
import de.adito.git.impl.data.*;
import de.adito.git.impl.data.diff.FileContentInfoImpl;
import de.adito.git.impl.data.diff.FileDiffImpl;
import de.adito.git.impl.data.diff.MergeDataImpl;
import de.adito.git.impl.revfilters.StashCommitFilter;
import de.adito.git.impl.util.GitRawTextComparator;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adito.git.api.data.IConfig.REMOTE_SECTION_KEY;
import static de.adito.git.api.data.IConfig.REMOTE_URL_KEY;
import static de.adito.git.impl.Util.getRelativePath;

public class RepositoryImplHelper
{

  private RepositoryImplHelper()
  {
  }

  /**
   * @param pGit        Git object to call for retrieving commits/objects/info about the repository status
   * @param pIdentifier Id of the commit to be retrieved
   * @return RevCommit that matches the passed identifier
   * @throws IOException if JGit encountered an error condition
   */
  static RevCommit getRevCommit(@NotNull Git pGit, String pIdentifier) throws IOException
  {
    try (RevWalk revWalk = new RevWalk(pGit.getRepository()))
    {
      ObjectId commitId = ObjectId.fromString(pIdentifier);
      return revWalk.parseCommit(commitId);
    }
  }

  /**
   * @param pGit    Git object to call for retrieving commits/objects/info about the repository status
   * @param pBranch String with the name of the branch for which the remote tracking branch should be fetched, pass null for the current branch
   * @return String with the name of the remote branch that the current branch is tracking
   * @throws IOException if an exception occurs while JGit is reading the git config file
   */
  @Nullable
  static String getRemoteTrackingBranch(@NotNull Git pGit, @Nullable String pBranch) throws IOException
  {
    return new BranchConfig(pGit.getRepository().getConfig(), pBranch == null ? pGit.getRepository().getBranch() : pBranch).getRemoteTrackingBranch();
  }

  /**
   * @param pGit         Git object to call for retrieving commits/objects/info about the repository status
   * @param pCurrentId   Id of the current branch/commit/file
   * @param pCompareToId Id of the branch/commit/file to be compared with the current one
   * @return List<DiffEntry> with the DiffEntrys that make the difference between the two commits/branches/files
   * @throws AditoGitException if JGit encountered an error condition
   */
  static List<DiffEntry> doDiff(@NotNull Git pGit, @NotNull ObjectId pCurrentId, @Nullable ObjectId pCompareToId) throws AditoGitException
  {
    try
    {
      AbstractTreeIterator oldTreeIter;
      if (pCompareToId == null || pCompareToId.toString().equals(CommitImpl.VOID_COMMIT.getId()))
        oldTreeIter = new EmptyTreeIterator();
      else
        oldTreeIter = prepareTreeParser(pGit.getRepository(), pCompareToId);
      CanonicalTreeParser newTreeIter = prepareTreeParser(pGit.getRepository(), pCurrentId);
      DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
      df.setRepository(pGit.getRepository());
      df.setDetectRenames(true);
      df.setDiffComparator(GitRawTextComparator.getCurrent().getValue());
      return df.scan(oldTreeIter, newTreeIter);
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  static List<IBranch> branchList(@NotNull Git pGit, @NotNull TrackedBranchStatusCache pTrackedBranchStatusCache)
  {
    ListBranchCommand listBranchCommand = pGit.branchList().setListMode(ListBranchCommand.ListMode.ALL);
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
      refBranchList.forEach(branch -> {
        if (!"refs/remotes/origin/HEAD".equals(branch.getName()))
          branches.add(new BranchImpl(branch, pTrackedBranchStatusCache));
      });
    }
    return branches;
  }

  static IFileStatus status(@NotNull Git pGit)
  {
    StatusCommand statusCommand = pGit.status();
    Status currentStatus;
    try
    {
      currentStatus = statusCommand.call();
    }
    catch (GitAPIException e)
    {
      throw new RuntimeException(e);
    }
    return new FileStatusImpl(currentStatus, pGit.getRepository().getDirectory());
  }

  static Optional<IRepositoryState> currentState(@NotNull Git pGit, @NotNull Function<String, IBranch> pGetBranchFunction,
                                                 @NotNull TrackedBranchStatusCache pTrackedBranchStatusCache)
  {
    try
    {
      String branch = pGit.getRepository().getFullBranch();
      if (branch == null)
        return Optional.empty();
      String remoteTrackingBranchName = getRemoteTrackingBranch(pGit, null);
      IBranch remoteTrackingBranch = null;
      if (remoteTrackingBranchName != null && pGit.getRepository().findRef(remoteTrackingBranchName) != null)
        remoteTrackingBranch = new BranchImpl(pGit.getRepository().findRef(remoteTrackingBranchName), pTrackedBranchStatusCache);
      List<String> remoteNames = new ArrayList<>(pGit.getRepository().getRemoteNames());
      if (pGit.getRepository().getRefDatabase().findRef(branch) == null)
      {
        return Optional.of(new RepositoryStateImpl(new BranchImpl(pGit.getRepository().resolve(branch), pTrackedBranchStatusCache), remoteTrackingBranch,
                                                   EnumMappings.mapRepositoryState(pGit.getRepository().getRepositoryState()), remoteNames));
      }
      return Optional.of(new RepositoryStateImpl(pGetBranchFunction.apply(branch), remoteTrackingBranch,
                                                 EnumMappings.mapRepositoryState(pGit.getRepository().getRepositoryState()), remoteNames));
    }
    catch (Exception e)
    {
      throw new RuntimeException("Ref isn't a Branch or a Commit to checkout", e);
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
  static CanonicalTreeParser prepareTreeParser(Repository pRepository, ObjectId pObjectId) throws IOException
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
   * @param pConflictingFile one of the Files with a conflict that were modified by git/JGit
   * @return id of the branch that gets merged ("THEIRS" for merges)
   */
  @Nullable
  static String getConflictingBranch(File pConflictingFile) throws IOException
  {
    try (Stream<String> lines = Files.lines(pConflictingFile.toPath()))
    {
      return lines.filter(line -> line.startsWith(">>>>>>>"))
          .findFirst()
          .map(s -> s.replace(">", "").trim())
          // may contain commit message, id of commit is then the part before the first blank
          .map(s -> {
            if (s.contains(" "))
              return s.split(" ")[0];
            return s;
          })
          .orElse(null);
    }
  }

  /**
   * @param pGit           Git object to call for retrieving commits/objects/info about the repository status
   * @param pConflicts     Strings of files that are in conflict
   * @param pStashCommitId id of the stashed commit that had a conflict
   * @param pDiffFn        BiFunction for diffing two commits
   * @return List of IMergeData with the diffs of merge-base to the two heads
   * @throws IOException       JGit exception
   * @throws AditoGitException if no commit fitting the ID can be found
   */
  @NotNull
  static List<IMergeData> getStashConflictMerge(@NotNull Git pGit, @NotNull Set<String> pConflicts, String pStashCommitId,
                                                @NotNull BiFunction<ICommit, ICommit, List<IFileDiff>> pDiffFn)
      throws IOException, AditoGitException
  {
    RevCommit toUnstash = getStashedCommit(pGit, pStashCommitId);
    if (toUnstash == null)
      throw new AditoGitException("could not find any stashed commits while trying to resolve conflict of stashed commit with HEAD");
    RevCommit headCommit = pGit.getRepository().parseCommit(pGit.getRepository().resolve(Constants.HEAD));
    RevCommit mergeBase = RepositoryImplHelper.getMergeBase(pGit, toUnstash,
                                                            headCommit);
    List<IMergeData> mergeConflicts = new ArrayList<>();
    ICommit stashCommit = new CommitImpl(toUnstash);
    ICommit parentBranchCommit = new CommitImpl(headCommit);
    List<IFileDiff> parentDiffList = pDiffFn.apply(parentBranchCommit, new CommitImpl(mergeBase));
    List<IFileDiff> toMergeDiffList = pDiffFn.apply(stashCommit, new CommitImpl(mergeBase));
    for (IFileDiff parentDiff : parentDiffList)
    {
      // may be empty, because JGit unstash doesn't apply changes if a conflict arises during unstashing. Ignore conflicting file status then
      if (pConflicts.isEmpty() || pConflicts.contains(parentDiff.getFileHeader().getFilePath()))
      {
        for (IFileDiff toMergeDiff : toMergeDiffList)
        {
          if (toMergeDiff.getFileHeader().getFilePath().equals(parentDiff.getFileHeader().getFilePath()))
          {
            mergeConflicts.add(_createMergeData(parentDiff, toMergeDiff));
          }
        }
      }
    }
    return mergeConflicts;
  }

  @NotNull
  static List<ICommit> getStashedCommits(Git pGit) throws AditoGitException
  {
    List<ICommit> stashedCommits = new ArrayList<>();
    try
    {
      pGit.stashList().call().forEach(pRevCommit -> stashedCommits.add(new CommitImpl(pRevCommit)));
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
    return stashedCommits;
  }

  /**
   * @param pGit           Git object to call for retrieving commits/objects/info about the repository status
   * @param pStashCommitId id of the stashed commit that should be retrieved from the stash
   * @return RevCommit with the specified id or null if no matching stashed commit could be found
   * @throws AditoGitException if JGit encounters an error
   */
  static RevCommit getStashedCommit(@NotNull Git pGit, @NotNull String pStashCommitId) throws AditoGitException
  {
    Collection<RevCommit> stashList;
    try
    {
      stashList = pGit.stashList().call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
    for (RevCommit stashedCommit : stashList)
    {
      if (stashedCommit.getName().equals(pStashCommitId))
      {
        return stashedCommit;
      }
    }
    return null;
  }

  /**
   * @param pGit           Git object to call for retrieving commits/objects/info about the repository status
   * @param pStashCommitId sha-1 id of the stashed commit for which to search the stack index for
   * @return index of the specified commit in the stash stack, or -1 if no commit with the passed ID can be found
   * @throws AditoGitException if JGit encounters an error
   */
  static int getStashIndexForId(@NotNull Git pGit, @NotNull String pStashCommitId) throws AditoGitException
  {
    int index = 0;
    boolean commitExists = false;
    Collection<RevCommit> stashList;
    try
    {
      stashList = pGit.stashList().call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
    for (RevCommit stashedCommit : stashList)
    {
      if (stashedCommit.getName().equals(pStashCommitId))
      {
        commitExists = true;
        break;
      }
      else
        index++;
    }
    return commitExists ? index : -1;
  }

  /**
   * @param pGit           Git object to call for retrieving commits/objects/info about the repository status
   * @param pCurrentBranch Identifier for the current branch
   * @param pBranchToMerge Identifier for the branch that should be merged into the current one
   * @param pForkCommit    the commit where the branches of the two commits diverged
   * @param pConflicts     Set of Strings (filePaths) that give the files with conflicts that occurred during the merge
   * @return List<IMergeData> describing the changes from the fork commit to each branch
   * @throws AditoGitException if JGit encountered an error condition
   */
  @NotNull
  static List<IMergeData> getMergeConflicts(@NotNull Git pGit, @NotNull String pCurrentBranch, @NotNull String pBranchToMerge,
                                            @NotNull ICommit pForkCommit, @NotNull Set<String> pConflicts,
                                            @NotNull BiFunction<ICommit, ICommit, List<IFileDiff>> pDiffFunction) throws AditoGitException
  {
    List<IMergeData> mergeConflicts = new ArrayList<>();
    ICommit parentBranchCommit;
    ICommit toMergeCommit;
    try
    {
      parentBranchCommit = new CommitImpl(pGit.getRepository().parseCommit(pGit.getRepository().resolve(pCurrentBranch)));
      toMergeCommit = new CommitImpl(pGit.getRepository().parseCommit(pGit.getRepository().resolve(pBranchToMerge)));
    }
    catch (IOException e)
    {
      throw new AditoGitException(e);
    }
    List<IFileDiff> parentDiffList = pDiffFunction.apply(parentBranchCommit, pForkCommit);
    List<IFileDiff> toMergeDiffList = pDiffFunction.apply(toMergeCommit, pForkCommit);
    for (IFileDiff parentDiff : parentDiffList)
    {
      if (parentDiff.getFileHeader().getChangeType() != EChangeType.COPY && pConflicts
          .stream()
          .anyMatch(pConflictFile -> IFileDiff.isSameFile(pConflictFile, parentDiff)))
      {
        for (IFileDiff toMergeDiff : toMergeDiffList)
        {
          if (toMergeDiff.getFileHeader().getChangeType() != EChangeType.COPY && IFileDiff.isSameFile(toMergeDiff, parentDiff))
          {
            mergeConflicts.add(_createMergeData(parentDiff, toMergeDiff));
          }
        }
      }
    }
    return mergeConflicts;
  }

  /**
   * Takes the editLists of the two given IFileDiffs and combines them such that two conflicting deltas reference the same lines in the fork-point version
   *
   * @param pParentDiff  IFileDiff from current to fork-point
   * @param pToMergeDiff IFileDiff from branch to merge to fork-point
   * @return IMergeData constructed from the IFileDiffs
   */
  private static IMergeData _createMergeData(IFileDiff pParentDiff, IFileDiff pToMergeDiff)
  {
    if (pParentDiff instanceof FileDiffImpl && pToMergeDiff instanceof FileDiffImpl)
    {
      if (pParentDiff.getFileHeader().getChangeType() == EChangeType.ADD && pToMergeDiff.getFileHeader().getChangeType() == EChangeType.ADD)
      {
        return _createBothAddedMergeData(pParentDiff, pToMergeDiff);
      }
      EditList parentEditList = ((FileDiffImpl) pParentDiff).getEditList();
      EditList toMergeEditList = ((FileDiffImpl) pToMergeDiff).getEditList();
      MergeDataImpl.adjustEditListForMerge(parentEditList, toMergeEditList);
      return new MergeDataImpl(new FileDiffImpl(pParentDiff.getFileHeader(), parentEditList,
                                                pParentDiff.getFileContentInfo(EChangeSide.OLD), pParentDiff.getFileContentInfo(EChangeSide.NEW)),
                               new FileDiffImpl(pToMergeDiff.getFileHeader(), toMergeEditList,
                                                pToMergeDiff.getFileContentInfo(EChangeSide.OLD), pToMergeDiff.getFileContentInfo(EChangeSide.NEW)));
    }
    else
    {
      return new MergeDataImpl(pParentDiff, pToMergeDiff);
    }
  }

  /**
   * Builds a basic text that contains the lines that are the same in both NEW text sides, and uses that text as the OLD version in the merge
   * Should be used if both files have type ADD, because else there is only one big chunk from nothing to the new text in the merge dialog
   *
   * @param pParentDiff  IFileDiff from current to fork-point
   * @param pToMergeDiff IFileDiff from branch to merge to fork-point
   * @return created IMergeData with the artifical OLD text
   */
  @NotNull
  private static IMergeData _createBothAddedMergeData(IFileDiff pParentDiff, IFileDiff pToMergeDiff)
  {
    EditList changedLines = StandAloneDiffProviderImpl.getChangedLines(pParentDiff.getFileContentInfo(EChangeSide.NEW).getFileContent().get(),
                                                                       pToMergeDiff.getFileContentInfo(EChangeSide.NEW).getFileContent().get());
    List<String> lines = new ArrayList<>(Arrays.asList(pParentDiff.getFileContentInfo(EChangeSide.NEW).getFileContent().get().split("\n", -1)));
    HashSet<Integer> contestedLines = new HashSet<>();
    for (Edit edit : changedLines)
    {
      for (int index = edit.getBeginA(); index < edit.getEndA(); index++)
      {
        contestedLines.add(index);
      }
      if (edit.getEndA() == edit.getBeginA())
        contestedLines.add(edit.getEndA());
    }

    StringBuilder artificalOldVersion = new StringBuilder();
    for (int index = 0; index < lines.size(); index++)
    {
      if (!contestedLines.contains(index))
        artificalOldVersion.append(lines.get(index)).append("\n");
    }
    String artificialOldVersionStr = artificalOldVersion.delete(Math.max(0, artificalOldVersion.length() - 1), artificalOldVersion.length()).toString();
    EditList parentEditList = StandAloneDiffProviderImpl.getChangedLines(artificialOldVersionStr,
                                                                         pParentDiff.getFileContentInfo(EChangeSide.NEW).getFileContent().get());
    EditList toMergeEditList = StandAloneDiffProviderImpl.getChangedLines(artificialOldVersionStr,
                                                                          pToMergeDiff.getFileContentInfo(EChangeSide.NEW).getFileContent().get());
    IFileContentInfo oldParentFCI = new FileContentInfoImpl(() -> artificialOldVersionStr, pParentDiff.getFileContentInfo(EChangeSide.OLD).getEncoding());
    IFileContentInfo oldToMergeFCI = new FileContentInfoImpl(() -> artificialOldVersionStr, pToMergeDiff.getFileContentInfo(EChangeSide.OLD).getEncoding());
    return new MergeDataImpl(new FileDiffImpl(pParentDiff.getFileHeader(), parentEditList,
                                              oldParentFCI, pParentDiff.getFileContentInfo(EChangeSide.NEW)),
                             new FileDiffImpl(pToMergeDiff.getFileHeader(), toMergeEditList,
                                              oldToMergeFCI, pToMergeDiff.getFileContentInfo(EChangeSide.NEW)));
  }

  static boolean isValidCommit(@NotNull Git pGit, @NotNull ObjectId pMergeBaseId)
  {
    try
    {
      pGit.getRepository().getObjectDatabase().open(pMergeBaseId);
    }
    catch (MissingObjectException pE)
    {
      Logger.getLogger(RepositoryImplHelper.class.getName()).log(Level.WARNING, pE, () -> "Encountered bad object with ID" + ObjectId.toString(pMergeBaseId));
      return false;
    }
    catch (IOException pIOE)
    {
      Logger.getLogger(RepositoryImplHelper.class.getName()).log(Level.WARNING, pIOE, () -> "Failed tto resolve commit with ID" + ObjectId.toString(pMergeBaseId));
      return false;
    }
    return true;
  }

  /**
   * @param pGit               Git object to call for retrieving commits/objects/info about the repository status
   * @param pParentBranchName  name (not id) of the branch currently on
   * @param pForeignBranchName name (not id) of the branch (or id for a commit) for which to find the closest forkPoint to this branch
   * @return RevCommit that is the forkPoint between the two branches, or null if not available
   * @throws IOException if an error occurs during parsing
   */
  @Nullable
  static RevCommit findForkPoint(@NotNull Git pGit, String pParentBranchName, String pForeignBranchName) throws IOException
  {
    HashSet<ObjectId> parsedIds = new HashSet<>();
    try (RevWalk walk = new RevWalk(pGit.getRepository()))
    {
      RevCommit foreignCommit = walk.lookupCommit(pGit.getRepository().resolve(pForeignBranchName));
      LinkedList<RevCommit> parentsToParse = new LinkedList<>();
      parentsToParse.add(walk.lookupCommit(pGit.getRepository().resolve(pParentBranchName)));
      while (!parentsToParse.isEmpty())
      {
        parsedIds.add(parentsToParse.peekFirst().getId());
        RevCommit commit = parentsToParse.poll();
        // check if foreignCommit is reachable from the currently selected commit
        if (walk.isMergedInto(commit, foreignCommit))
        {
          // check if commit is a valid commit that does not contain errors when parsed
          walk.parseBody(commit);
          return commit;
        }
        else
        {
          List<ObjectId> filteredCommitParents = Arrays.stream(commit.getParents())
              .map(RevObject::getId)
              .filter(pObjectId -> !parsedIds.contains(pObjectId))
              .collect(Collectors.toList());
          addCommitsSortedByTime(filteredCommitParents, parentsToParse, walk);
          parsedIds.addAll(filteredCommitParents);
        }
      }
    }
    return null;
  }

  /**
   * Add all commits from list pCommits to pParentsList, sorted by time
   *
   * @param pCommits    list of objectIds representing commits to add to the other list. Does not have to be sorted and will not be changed
   * @param pParentList list of commits to add to, has to be pre-sorted in descending order (the latest commits come first). This list will be changed, and it is best if
   *                    this list is a linked list (complexity on insert)
   * @param pRevWalk    RevWalk that allows a lookup of the commits that are associated with ObjectIds
   */
  private static void addCommitsSortedByTime(@NotNull List<ObjectId> pCommits, @NotNull List<RevCommit> pParentList, @NotNull RevWalk pRevWalk)
  {
    pCommits.stream().map(pRevWalk::lookupCommit).forEach(pCommit -> addCommitByTime(pCommit, pParentList));
  }

  /**
   * Insert the commit into a pre-sorted list, according to commit time
   *
   * @param pCommit     Commit to insert into the list
   * @param pCommitList List of Commits, will be changed -> has to be mutable. Also has to be sorted for this method to work (sorted by commit time, latest commits come first)
   */
  @VisibleForTesting
  static void addCommitByTime(@NotNull RevCommit pCommit, @NotNull List<RevCommit> pCommitList)
  {
    ListIterator<RevCommit> commitListIterator = pCommitList.listIterator();
    while (commitListIterator.hasNext())
    {
      RevCommit next = commitListIterator.next();
      if (next.getCommitTime() < pCommit.getCommitTime())
      {
        commitListIterator.previous();
        commitListIterator.add(pCommit);
        return;
      }
    }
    pCommitList.add(pCommit);
  }

  /**
   * Returns all commits matching the criteria, always filters out any stashed commits
   *
   * @param pGit          Git object to call for retrieving commits/objects/info about the repository status
   * @param pCommitFilter filter that defines which commits are considered as result
   * @return List of ICommits matching the provided criteria
   * @throws AditoGitException if JGit throws an exception/returns null
   */
  @NotNull
  static DAGFilterIterator<ICommit> getCommits(@NotNull Git pGit, @NotNull ICommitFilter pCommitFilter) throws AditoGitException
  {
    try
    {
      Iterable<RevCommit> refCommits;
      LogCommand logCommand = pGit.log();
      RevFilter revFilter = new StashCommitFilter(pGit);
      if (pCommitFilter.getBranch() != null && !pCommitFilter.getBranch().equals(IBranch.ALL_BRANCHES))
      {
        if (pCommitFilter.getBranch().equals(IBranch.HEAD))
          logCommand.add(pGit.getRepository().resolve("HEAD"));
        else
          logCommand.add(pGit.getRepository().resolve(pCommitFilter.getBranch().getName()));
      }
      else
      {
        logCommand.all();
      }
      if (!pCommitFilter.getFiles().isEmpty())
      {
        pCommitFilter.getFiles().forEach(pFile -> logCommand.addPath(getRelativePath(pFile, pGit)));
      }
      logCommand.setRevFilter(revFilter);
      refCommits = logCommand.call();
      Function<RevCommit, ICommit> transformFn = CommitImpl::new;
      return new DAGFilterIterator<>(Iterators.transform(refCommits.iterator(), transformFn::apply), pCommitFilter);
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException("Unable to check the commits of one branch and/or file. Branch: " + pCommitFilter.getBranch()
                                      + ", File: " + pCommitFilter.getFiles(), pE);
    }
  }

  /**
   * Searches a merge base for two given commits
   *
   * @param pGit         Git object to call for retrieving commits/objects/info about the repository status
   * @param pYourCommit  the commit on the YOURS side
   * @param pTheirCommit the commit on the THEIRS side
   * @return RevCommit that forms the merge-base for yourCommit and theirCommit
   * @throws IOException if any errors occur during the RevWalk
   */
  static RevCommit getMergeBase(Git pGit, RevCommit pYourCommit, RevCommit pTheirCommit) throws IOException
  {
    RevWalk walk = new RevWalk(pGit.getRepository());
    walk.setRevFilter(RevFilter.MERGE_BASE);
    walk.markStart(pYourCommit);
    walk.markStart(pTheirCommit);
    return walk.next();
  }

  /**
   * Figures out the name of the remote by using the tracked branch of the currently active branch, or the remote of master if the current branch does not have a
   * tracked branch
   *
   * @return name of the remote
   * @throws IOException if an exception occurs while JGit is reading the git config file
   */
  @Nullable
  public static String getRemoteName(@NotNull Git pGit, @Nullable String pRemoteUrl) throws IOException
  {
    String remoteName = null;
    if (pRemoteUrl != null)
    {
      remoteName = pGit.getRepository().getRemoteNames()
          .stream()
          .filter(pRemote -> pGit.getRepository().getConfig().getString(REMOTE_SECTION_KEY, pRemote, REMOTE_URL_KEY).equals(pRemoteUrl))
          .findFirst()
          .orElse(null);
    }
    return remoteName;
  }

  static File getRebaseMergeHead(@NotNull Git pGit)
  {
    return new File(pGit.getRepository().getDirectory().getAbsolutePath(), "rebase-merge/head");
  }
}
