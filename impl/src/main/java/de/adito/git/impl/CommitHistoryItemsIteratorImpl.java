package de.adito.git.impl;

import de.adito.git.api.*;
import de.adito.git.api.dag.IDAGFilterIterator;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.ITag;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Iterator that transforms elements from an Iterator with commits into commitHistoryTreeListItems
 *
 * @author m.kaspera, 24.05.2019
 */
public class CommitHistoryItemsIteratorImpl implements ICommitHistoryItemsIterator
{

  private final ColorRoulette colorRoulette = ColorRoulette.create();
  private final IDAGFilterIterator<ICommit> commitFilterIter;
  private final List<IBranch> allBranches;
  private final List<ITag> allTags;
  private ICommit currentCommit = null;
  private CommitHistoryTreeListItem latestHistoryItem = null;
  private boolean encounteredLast = false;

  public CommitHistoryItemsIteratorImpl(IDAGFilterIterator<ICommit> pCommitFilterIter, List<IBranch> allBranches, List<ITag> allTags)
  {
    commitFilterIter = pCommitFilterIter;
    this.allBranches = allBranches;
    this.allTags = allTags;
  }

  @Override
  public boolean hasNext()
  {
    // while loop in case of null elements in the iterator
    while (currentCommit == null && commitFilterIter.hasNext())
    {
      currentCommit = commitFilterIter.next();
    }
    if (currentCommit != null && commitFilterIter.hasNext())
      return true;
    else if (!encounteredLast)
    {
      encounteredLast = true;
      return true;
    }
    return false;
  }

  @Override
  public CommitHistoryTreeListItem next()
  {
    while (currentCommit == null && commitFilterIter.hasNext())
    {
      currentCommit = commitFilterIter.next();
    }
    ICommit bufferedCommit = null;
    while (commitFilterIter.hasNext() && bufferedCommit == null)
    {
      bufferedCommit = commitFilterIter.next();
    }
    CommitHistoryTreeListItem commitHistoryTreeListItem = _createAncestryLines(bufferedCommit);

    currentCommit = bufferedCommit;
    latestHistoryItem = commitHistoryTreeListItem;
    return commitHistoryTreeListItem;
  }

  @Override
  public @NotNull List<CommitHistoryTreeListItem> tryReadEntries(int pNumEntries)
  {
    List<CommitHistoryTreeListItem> entries = new ArrayList<>(pNumEntries);
    for (int index = 0; index < pNumEntries && hasNext(); index++)
    {
      entries.add(next());
    }
    return entries;
  }

  private CommitHistoryTreeListItem _createAncestryLines(ICommit pBufferdCommit)
  {
    // signifies if any of the already processed AncestryLines had the current commit as parent/next commit
    boolean processedParents = false;
    // if this is still true after the for loop no AncestryLine had the current commit as parent -> new Branch
    boolean isBranchHead = true;
    // list of AncestryLines after this commit
    List<AncestryLine> newLines = new ArrayList<>();
    // List of AncestryLines that come from the parent commits of the current commit
    List<AncestryLine> parentLines = new ArrayList<>();
    // the first AncestryLine that had the current commit as next commit
    AncestryLine advancedLine = null;
    if (latestHistoryItem != null)
    {
      for (AncestryLine formerLine : latestHistoryItem.getAncestryLines())
      {
        if (formerLine.getNextCommit().equals(currentCommit))
        {
          if (!processedParents && formerLine.getLineType() != AncestryLine.LineType.STILLBORN)
          {
            parentLines = _getParentLines(formerLine.getColor());
            newLines.addAll(parentLines);
            advancedLine = formerLine;
            processedParents = true;
          }
          else
          {
            colorRoulette.returnColor(formerLine.getColor());
          }
          isBranchHead = false;
        }
        else
        {
          if (formerLine.getLineType() == AncestryLine.LineType.INFANT)
          {
            // Infant lines become full lines
            formerLine.setLineType(AncestryLine.LineType.FULL);
            newLines.add(formerLine);
          }
          // stillborn lines do not continue, all other lines do
          else if (formerLine.getLineType() != AncestryLine.LineType.STILLBORN)
          {
            newLines.add(formerLine);
          }
        }
      }
      if (isBranchHead)
      {
        advancedLine = new AncestryLine(pBufferdCommit, colorRoulette.get(), AncestryLine.LineType.INFANT);
        newLines.add(advancedLine);
      }
    }
    else
    {
      // start from scratch and create AncestryLine from the very first commmit
      advancedLine = new AncestryLine(pBufferdCommit, colorRoulette.get(), AncestryLine.LineType.FULL);
      newLines.add(advancedLine);
    }
    // check for stillborns
    if (parentLines.size() > 1)
    {
      _checkForStillborn(pBufferdCommit, newLines, parentLines, advancedLine);
    }

    HistoryGraphElement historyGraphElement = new HistoryGraphElement();
    historyGraphElement.calculateUpperLines(latestHistoryItem == null ? List.of() : latestHistoryItem.getAncestryLines(), advancedLine, currentCommit);
    historyGraphElement.calculateLowerLines(newLines);
    return new CommitHistoryTreeListItem(currentCommit, newLines, historyGraphElement, allBranches, allTags);
  }

  /**
   * checks if any of the parent lines is stillborn
   *
   * @param pBufferdCommit next commit in the list
   * @param pNewLines      List of AncestryLines after the current commit
   * @param pParentLines   List of AncestryLines that "spawned" from the knot of the current commit
   * @param pAdvancedLine  the AncestryLine that lead to the knot of the current commit
   */
  private void _checkForStillborn(ICommit pBufferdCommit, List<AncestryLine> pNewLines, List<AncestryLine> pParentLines, AncestryLine pAdvancedLine)
  {
    // the first parentLine continues the AncestryLine and is therefore by default not stillborn -> start at index 1
    for (int parentLIndex = 1; parentLIndex < pParentLines.size(); parentLIndex++)
    {
      int fullLineCount = 0;
      for (int index = 0; index < pNewLines.size(); index++)
      {
        if (pNewLines.indexOf(pParentLines.get(parentLIndex)) != index
            && pParentLines.get(parentLIndex).getNextCommit().equals(pNewLines.get(index).getNextCommit())
            && pParentLines.get(parentLIndex).getNextCommit().equals(pBufferdCommit))
        {
          double stillBornMeetingIndex = _getStillbornMeetingOffset(fullLineCount, pAdvancedLine);
          pParentLines.get(parentLIndex).setLineType(AncestryLine.LineType.STILLBORN);
          pParentLines.get(parentLIndex).setStillBornMeetingIndex(stillBornMeetingIndex);
          break;
        }
        if (pNewLines.get(index).getLineType() == AncestryLine.LineType.FULL)
          fullLineCount++;
      }
    }
  }

  /**
   * @param pIndex        number of ancestryLines of type full that come before the stillborn line begins
   * @param pAdvancedLine the ancestryLine that the current commit was on
   * @return index at which the line for the stillborn intersects the bootom of the table cell
   */
  private double _getStillbornMeetingOffset(int pIndex, AncestryLine pAdvancedLine)
  {
    return (double) (pIndex + latestHistoryItem.getAncestryLines()
        .subList(0, latestHistoryItem.getAncestryLines().indexOf(pAdvancedLine))
        .stream()
        .filter(pLine -> pLine.getLineType() == AncestryLine.LineType.FULL)
        .count())
        / 2;
  }

  /**
   * @param pParentLineColor Color that the AncestryLine leading to the current commit had
   * @return AncestryLines that spawn from the current commit
   */
  private List<AncestryLine> _getParentLines(Color pParentLineColor)
  {
    ArrayList<AncestryLine> parentLines = new ArrayList<>();
    if (!currentCommit.getParents().isEmpty())
    {
      // The parent forms the line that "goes on" with the same color as the line that lead to the current commit
      parentLines.add(new AncestryLine(currentCommit.getParents().get(0), pParentLineColor, AncestryLine.LineType.FULL));
      if (currentCommit.getParents().size() > 1)
      {
        for (int parentIndex = 1; parentIndex < currentCommit.getParents().size(); parentIndex++)
        {
          parentLines.add(new AncestryLine(currentCommit.getParents().get(parentIndex), colorRoulette.get(), AncestryLine.LineType.INFANT));
        }
      }
    }
    return parentLines;
  }

}
