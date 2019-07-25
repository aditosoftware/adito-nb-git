package de.adito.git.impl;

import com.google.common.collect.Iterators;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.impl.dag.DAGFilterIterator;
import de.adito.git.impl.data.*;
import de.adito.git.impl.revfilters.StashCommitFilter;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adito.git.api.data.IConfig.REMOTE_URL_KEY;
import static de.adito.git.api.data.IConfig.SSH_SECTION_KEY;
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
  static List<DiffEntry> doDiff(@NotNull Git pGit, ObjectId pCurrentId, ObjectId pCompareToId) throws AditoGitException
  {
    try
    {
      CanonicalTreeParser oldTreeIter = prepareTreeParser(pGit.getRepository(), pCompareToId);
      CanonicalTreeParser newTreeIter = prepareTreeParser(pGit.getRepository(), pCurrentId);
      DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
      df.setRepository(pGit.getRepository());
      df.setDetectRenames(true);
      return df.scan(oldTreeIter, newTreeIter);
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  static List<IBranch> branchList(@NotNull Git pGit)
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
      refBranchList.forEach(branch -> branches.add(new BranchImpl(branch)));
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

  static Optional<IRepositoryState> currentState(@NotNull Git pGit, Function<String, IBranch> pGetBranchFunction)
  {
    try
    {
      String branch = pGit.getRepository().getFullBranch();
      if (branch == null)
        return Optional.empty();
      String remoteTrackingBranchName = getRemoteTrackingBranch(pGit, null);
      IBranch remoteTrackingBranch = null;
      if (remoteTrackingBranchName != null)
        remoteTrackingBranch = new BranchImpl(pGit.getRepository().resolve(remoteTrackingBranchName));
      List<String> remoteNames = new ArrayList<>(pGit.getRepository().getRemoteNames());
      if (pGit.getRepository().getRefDatabase().findRef(branch) == null)
      {
        return Optional.of(new RepositoryStateImpl(new BranchImpl(pGit.getRepository().resolve(branch)), remoteTrackingBranch,
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
   * @return List of IMergeDiff with the diffs of merge-base to the two heads
   * @throws IOException       JGit exception
   * @throws AditoGitException if no commit fitting the ID can be found
   */
  @NotNull
  static List<IMergeDiff> getStashConflictMerge(@NotNull Git pGit, @NotNull Set<String> pConflicts, String pStashCommitId,
                                                @NotNull BiFunction<List<File>, ICommit, List<IFileDiff>> pLocalDiffFn,
                                                @NotNull BiFunction<ICommit, ICommit, List<IFileDiff>> pDiffFn)
      throws IOException, AditoGitException
  {
    RevCommit toUnstash = getStashedCommit(pGit, pStashCommitId);
    if (toUnstash == null)
      throw new AditoGitException("could not find any stashed commits while trying to resolve conflict of stashed commit with HEAD");
    RevCommit mergeBase = RepositoryImplHelper.getMergeBase(pGit, toUnstash,
                                                            pGit.getRepository().parseCommit(pGit.getRepository().resolve(Constants.HEAD)));
    List<IMergeDiff> mergeConflicts = new ArrayList<>();
    ICommit parentBranchCommit;
    try
    {
      parentBranchCommit = new CommitImpl(pGit.getRepository().parseCommit(pGit.getRepository().resolve(toUnstash.getName())));
    }
    catch (IOException e)
    {
      throw new AditoGitException(e);
    }
    List<IFileDiff> parentDiffList = pDiffFn.apply(parentBranchCommit, new CommitImpl(mergeBase));
    List<IFileDiff> toMergeDiffList = pLocalDiffFn.apply(null, new CommitImpl(mergeBase));
    for (IFileDiff parentDiff : parentDiffList)
    {
      // may be empty, because JGit unstash doesn't apply changes if a conflict arises during unstashing. Ignore conflicting file status then
      if (pConflicts.isEmpty() || pConflicts.contains(parentDiff.getFilePath()))
      {
        for (IFileDiff toMergeDiff : toMergeDiffList)
        {
          if (toMergeDiff.getFilePath().equals(parentDiff.getFilePath()))
          {
            mergeConflicts.add(new MergeDiffImpl(parentDiff, toMergeDiff));
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
   * @return List<IMergeDiff> describing the changes from the fork commit to each branch
   * @throws AditoGitException if JGit encountered an error condition
   */
  @NotNull
  static List<IMergeDiff> getMergeConflicts(@NotNull Git pGit, String pCurrentBranch, String pBranchToMerge,
                                            ICommit pForkCommit, Set<String> pConflicts,
                                            BiFunction<ICommit, ICommit, List<IFileDiff>> pDiffFunction) throws AditoGitException
  {
    List<IMergeDiff> mergeConflicts = new ArrayList<>();
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
      if (pConflicts.stream().anyMatch(pConflictFile -> IFileDiff.isSameFile(pConflictFile, parentDiff)))
      {
        for (IFileDiff toMergeDiff : toMergeDiffList)
        {
          if (IFileDiff.isSameFile(toMergeDiff, parentDiff))
          {
            mergeConflicts.add(new MergeDiffImpl(parentDiff, toMergeDiff));
          }
        }
      }
    }
    return mergeConflicts;
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
    try (RevWalk walk = new RevWalk(pGit.getRepository()))
    {
      RevCommit foreignCommit = walk.lookupCommit(pGit.getRepository().resolve(pForeignBranchName));
      LinkedList<ObjectId> parentsToParse = new LinkedList<>();
      parentsToParse.add(pGit.getRepository().resolve(pParentBranchName));
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
          .filter(pRemote -> pGit.getRepository().getConfig().getString(SSH_SECTION_KEY, pRemote, REMOTE_URL_KEY).equals(pRemoteUrl))
          .findFirst()
          .orElse(null);
    }
    if (remoteName != null)
      return remoteName;
    String remoteTrackingBranch = RepositoryImplHelper.getRemoteTrackingBranch(pGit, null);
    // Fallback: get remoteBranch of master and resolve remoteName with that branch
    if (remoteTrackingBranch == null)
    {
      return pGit.getRepository().getRemoteName(remoteTrackingBranch);
    }
    return null;
  }

  static File getRebaseMergeHead(@NotNull Git pGit)
  {
    return new File(pGit.getRepository().getDirectory().getAbsolutePath(), "rebase-merge/head");
  }
}
