package de.adito.git.api;

import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.ITag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the AncestryLines and their drawing coordinates for a commit
 * This allows a display of the branching and merging of the commits over time
 * <p>
 * The next item further down the list can be retrieved by calling the nextItem
 * method on this object and supplying it with the requested coordinates
 * <p>
 * Lo' and despair
 *
 * @author m.kaspera 16.11.2018
 */
public class CommitHistoryTreeListItem
{

  private final ICommit commit;
  private final List<IBranch> branches;
  private final List<ITag> tags;
  private final List<AncestryLine> ancestryLines;
  private final HistoryGraphElement historyGraphElement;
  private final int maxLineWidth;

  /**
   * Stores the AncestryLines and their drawing coordinates for a commit
   *
   * @param pCommit              the Commit around which this object holds information
   * @param pAncestryLines       AncestryLines leading up to this commit
   * @param pHistoryGraphElement HistoryGraphElement that stores information about how to draw the commitHistoryGraph for this item
   * @param pAllBranches         List of all IBranches
   * @param pAllTags             List of all ITags in the repository
   */
  public CommitHistoryTreeListItem(@NotNull ICommit pCommit, @NotNull List<AncestryLine> pAncestryLines, HistoryGraphElement pHistoryGraphElement,
                                   @NotNull List<IBranch> pAllBranches, List<ITag> pAllTags)
  {
    commit = pCommit;
    ancestryLines = pAncestryLines;
    historyGraphElement = pHistoryGraphElement;
    branches = _getBelongingBranches(pAllBranches);
    tags = _getBelongingTags(pAllTags);
    maxLineWidth = historyGraphElement.calculateMaxLineWidth();
  }

  private List<ITag> _getBelongingTags(List<ITag> pAllTags)
  {
    List<ITag> belongingTags = new ArrayList<>();
    for (ITag tag : pAllTags)
    {
      if (tag.getId().equals(commit.getId()))
        belongingTags.add(tag);
    }
    return belongingTags;
  }

  private List<IBranch> _getBelongingBranches(List<IBranch> pAllBranches)
  {
    List<IBranch> belongingBranches = new ArrayList<>();
    for (IBranch branch : pAllBranches)
    {
      if (branch.getId().equals(commit.getId()))
        belongingBranches.add(branch);
    }
    return belongingBranches;
  }

  @Override
  public String toString()
  {
    return "commitId: " + commit.getId() + ", message: " + commit.getShortMessage();
  }

  /**
   * @return the commit for which the AncestryLines were gathered
   */
  public ICommit getCommit()
  {
    return commit;
  }

  /**
   * @return List of IBranches that point to this commit
   */
  public List<IBranch> getBranches()
  {
    return branches;
  }

  /**
   * @return List of ITags that point to this commit
   */
  public List<ITag> getTags()
  {
    return tags;
  }

  /**
   * @return the List of AncestryLines that are running for this commit
   */
  public List<AncestryLine> getAncestryLines()
  {
    return ancestryLines;
  }

  /**
   * @return List with ColoredLineCoordinates for the renderer to draw
   */
  public List<HistoryGraphElement.ColoredLineCoordinates> getLinesToDraw()
  {
    return historyGraphElement.getLineCoordinates();
  }

  /**
   * @return the KnotCoordinates for the renderer to draw
   */
  public HistoryGraphElement.KnotCoordinates getKnotCoordinates()
  {
    return historyGraphElement.getKnotCoordinates();
  }

  /**
   * @return the amount of space/width that the lines for this CHTLI need
   */
  public int getMaxLineWidth()
  {
    return maxLineWidth;
  }

  /**
   * Check if the commit, tags and branches of this and pOther are the same. Does not take into account the AncestryLines (so do not use this to check if a list contains
   * the same CHTLI if you are also interested in the position of the CHTLIs, only use in a list if the position does not matter to you or you are comparing the
   * same indices in the list)
   *
   * @param pOther the second CommitHistoryTreeListItem
   * @return true if the commit, the tags and the branches referenced by the other CHTLI are the same as those referenced by this
   */
  public boolean commitDetailsEquals(CommitHistoryTreeListItem pOther)
  {
    if (!pOther.getCommit().equals(getCommit()))
      return false;
    if (!pOther.getTags().equals(getTags()))
      return false;
    return pOther.getBranches().equals(getBranches());
  }

}
