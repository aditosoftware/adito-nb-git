package de.adito.git.impl.revfilters;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RevFilter that filters out all stash commits. This is not as simple as it sounds, as a stash command can lead to up to three stash commits being
 * created (one for the working tree, one for the index, one for ignored files) and only the last of these commits is explicitly tagged as stash
 * commit.
 *
 * @author m.kaspera, 13.02.2019
 */
public class StashCommitFilter extends RevFilter
{

  private final Git git;
  private final ArrayList<ObjectId> stashedCommitIdList = new ArrayList<>();
  private RevCommit latestStashedCommit = null;

  public StashCommitFilter(@NotNull Git pGit)
  {
    git = pGit;
    try
    {
      pGit.reflog().setRef("refs/stash").call().iterator()
          .forEachRemaining(pReflogEntry -> stashedCommitIdList.add((pReflogEntry.getNewId())));
    }
    catch (RefNotFoundException pRefNotFound)
    {
      // do nothing, no stash commits exits and the list is empty
    }
    catch (GitAPIException pE)
    {
      Logger.getLogger(StashCommitFilter.class.getName()).log(Level.WARNING, pE, () -> "Error while initialising the StashCommitFilter");
    }
  }

  @Override
  public boolean include(RevWalk pWalker, RevCommit pCommit)
  {
    if (stashedCommitIdList.isEmpty())
      return true;
    // if commit is explicitly in the stash branch, or if it is the parent of the last explicit stash (since we're going backwards in time) and has
    // the same commit time it is considered a stash commit
    if (stashedCommitIdList.contains(pCommit.getId())
        ||
        (latestStashedCommit != null
            && Arrays.asList(latestStashedCommit.getParents()).contains(pCommit)
            && latestStashedCommit.getCommitTime() == pCommit.getCommitTime()))
    {
      latestStashedCommit = pCommit;
      return false;
    }
    return true;
  }

  @Override
  public RevFilter clone()
  {
    return new StashCommitFilter(git);
  }
}
