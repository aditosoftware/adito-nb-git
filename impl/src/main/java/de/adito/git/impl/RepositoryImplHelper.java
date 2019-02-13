package de.adito.git.impl;

import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.impl.data.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
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
   * @param pGit Git object to call for retrieving commits/objects/info about the repository status
   * @return String with the name of the remote branch that the current branch is tracking
   * @throws IOException if an exception occurs while JGit is reading the git config file
   */
  public static String getRemoteTrackingBranch(@NotNull Git pGit) throws IOException
  {
    return new BranchConfig(pGit.getRepository().getConfig(), pGit.getRepository().getBranch()).getRemoteTrackingBranch();
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
      return pGit.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
    }
    catch (IOException | GitAPIException pE)
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

  static Optional<IBranch> currentBranch(@NotNull Git pGit, Function<String, Optional<IBranch>> pGetBranchFunction)
  {
    try
    {
      String branch = pGit.getRepository().getFullBranch();
      if (branch == null)
        return Optional.empty();
      if (pGit.getRepository().getRefDatabase().getRef(branch) == null)
      {
        return Optional.of(new BranchImpl(pGit.getRepository().resolve(branch)));
      }
      return Optional.ofNullable(pGetBranchFunction.apply(branch))
          .orElseThrow(() -> new AditoGitException("Unable to find branch for name " + branch));
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
                                                @NotNull BiFunction<ICommit, ICommit, List<IFileDiff>> pDiffFn)
      throws IOException, AditoGitException
  {
    RevCommit toUnstash = getStashedCommit(pGit, pStashCommitId);
    if (toUnstash == null)
      throw new AditoGitException("could not find any stashed commits while trying to resolve conflict of stashed commit with HEAD");
    RevCommit mergeBase = RepositoryImplHelper.getMergeBase(pGit, toUnstash,
                                                            pGit.getRepository().parseCommit(pGit.getRepository().resolve(Constants.HEAD)));
    return RepositoryImplHelper.getMergeConflicts(pGit, toUnstash.getName(),
                                                  ObjectId.toString(pGit.getRepository().resolve(Constants.HEAD)),
                                                  new CommitImpl(mergeBase), pConflicts, pDiffFn);
  }

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
      if (pConflicts.contains(parentDiff.getFilePath()))
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
   * @param pSourceBranch IBranch that the retrieved commits should be from
   * @param pFile         File that is affected by all commits that are retrieved
   * @param pIndexFrom    number of (matching) commits to skip before retrieving commits from the log
   * @param pNumCommits   number of (matching) commits to retrieve
   * @return List of ICommits matching the provided criteria
   * @throws AditoGitException if JGit throws an exception/returns null
   */
  static List<ICommit> getCommits(@NotNull Git pGit, @Nullable IBranch pSourceBranch, @Nullable File pFile,
                                  int pIndexFrom, int pNumCommits) throws AditoGitException
  {
    try
    {
      List<ICommit> commitList = new ArrayList<>();
      Iterable<RevCommit> refCommits;
      LogCommand logCommand = pGit.log();
      RevFilter revFilter = new StashCommitFilter(pGit);
      if (pSourceBranch != null)
      {
        logCommand.add(pGit.getRepository().resolve(pSourceBranch.getName()));
      }
      else
      {
        logCommand.all();
      }
      if (pFile != null)
      {
        logCommand.addPath(getRelativePath(pFile, pGit));
      }
      if (pIndexFrom >= 0)
      {
        // Since setting an explicit revFilter overrides the skip and max options, create the filters and AND them together with our filter
        revFilter = AndRevFilter.create(revFilter, SkipRevFilter.create(pIndexFrom));
      }
      if (pNumCommits >= 0)
      {
        // Since setting an explicit revFilter overrides the skip and max options, create the filters and AND them together with our filter
        revFilter = AndRevFilter.create(revFilter, MaxCountRevFilter.create(pNumCommits));
      }
      logCommand.setRevFilter(revFilter);
      refCommits = logCommand.call();
      if (refCommits != null)
      {
        refCommits.forEach(commit -> commitList.add(new CommitImpl(commit)));
      }
      else
      {
        throw new AditoGitException("Object returned by JGit was null while trying to retrieve commits. Branch: "
                                        + pSourceBranch + ", File: " + pFile);
      }
      return commitList;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException("Unable to check the commits of one branch and/or file. Branch: " + pSourceBranch + ", File: " + pFile, pE);
    }
  }

  /**
   * Searches the causal chain of pThrowable and looks for any occurrences of pLookFor
   *
   * @param pThrowable The exception whose causal chain should be searched for pLookFor
   * @param pLookFor   The exception that should be searched in the causal chain of pThrowable
   * @param <EX>       Exception class
   * @return whether or not pLookFor is in the causal chain of pThrowable
   */
  static <EX extends Throwable> boolean containsCause(Throwable pThrowable, Class<EX> pLookFor)
  {
    return Throwables.getCausalChain(pThrowable).stream().anyMatch(Predicates.instanceOf(pLookFor)::apply);
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

}
