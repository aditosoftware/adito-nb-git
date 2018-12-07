package de.adito.git.impl;

import de.adito.git.api.AditoGitException;
import de.adito.git.api.data.*;
import de.adito.git.impl.data.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.adito.git.impl.Util.getRelativePath;

class RepositoryImplHelper
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

  static IBranch currentBranch(@NotNull Git pGit, Function<String, Optional<IBranch>> pGetBranchFunction)
  {
    try
    {
      String branch = pGit.getRepository().getFullBranch();
      if (pGit.getRepository().getRefDatabase().getRef(branch) == null)
      {
        return new BranchImpl(pGit.getRepository().resolve(branch));
      }
      return pGetBranchFunction.apply(branch).orElseThrow(() -> new AditoGitException("Unable to find branch for name " + branch));
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
      return lines.filter(line -> line.startsWith(">>>>>>>>"))
          .findFirst()
          .map(s -> s.replace(">", "").trim())
          .orElse(null);
    }
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
        logCommand.setSkip(pIndexFrom);
      }
      if (pNumCommits >= 0)
      {
        logCommand.setMaxCount(pNumCommits);
      }
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
   * Creates a new stash commit with all uncommitted changes in the index
   *
   * @param pGit Git object to call for retrieving commits/objects/info about the repository status
   * @return SHA-1 id of the created stash commit
   * @throws GitAPIException if JGit throws an exception
   */
  static String stashChanges(@NotNull Git pGit) throws GitAPIException
  {
    return pGit.stashCreate().call().getName();
  }

  /**
   * Checks whether there is a stashed commit available or not and un-stashes the commit if one exists
   *
   * @param pGit Git object to call for retrieving commits/objects/info about the repository status
   * @throws GitAPIException   if JGit throws an exception
   * @throws AditoGitException if a stashed commit exists in the list of stashed commits, but it cannot be retrieved by its ID
   */
  static void unStashIfAvailable(@NotNull Git pGit) throws GitAPIException, AditoGitException
  {
    Collection<RevCommit> stashedCommits = pGit.stashList().call();
    if (!stashedCommits.isEmpty())
    {
      unStashChanges(pGit, stashedCommits.iterator().next().getName());
    }
  }

  /**
   * @param pGit Git object to call for retrieving commits/objects/info about the repository status
   * @throws GitAPIException   if JGit throws an exception
   * @throws AditoGitException if there is no stashed commit available
   */
  static void unStashChange(@NotNull Git pGit) throws GitAPIException, AditoGitException
  {
    Collection<RevCommit> stashedCommits = pGit.stashList().call();
    if (!stashedCommits.isEmpty())
    {
      unStashChanges(pGit, stashedCommits.iterator().next().getName());
    }
    else
    {
      throw new AditoGitException("Could not find any stashed commits to un-stash");
    }
  }

  /**
   * Applies the changes stored in the stashed commit with id pStashCommitId and removes the
   * stashed commit afterwards
   *
   * @param pGit           Git object to call for retrieving commits/objects/info about the repository status
   * @param pStashCommitId id of the stashed commit to apply
   * @throws GitAPIException   if JGit throws an exception
   * @throws AditoGitException if no stashed commit can be found with id pStashCommitId
   */
  @SuppressWarnings("WeakerAccess")
  static void unStashChanges(@NotNull Git pGit, @NotNull String pStashCommitId) throws GitAPIException, AditoGitException
  {
    boolean stashCommitExists = false;
    int index = 0;
    for (RevCommit stashedCommit : pGit.stashList().call())
    {
      if (stashedCommit.getName().equals(pStashCommitId))
      {
        stashCommitExists = true;
        break;
      }
      else
        index++;
    }
    if (!stashCommitExists)
      throw new AditoGitException("Could not find a stashed commit for id " + pStashCommitId);
    pGit.stashApply().setStashRef(pStashCommitId).call();
    pGit.stashDrop().setStashRef(index).call();
  }

}
