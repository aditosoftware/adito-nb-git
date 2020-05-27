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

  public CommitHistoryItemsIteratorImpl(@NotNull IDAGFilterIterator<ICommit> pCommitFilterIter, @NotNull List<IBranch> allBranches, @NotNull List<ITag> allTags)
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
    else if (currentCommit != null && !encounteredLast)
    {
      encounteredLast = true;
      return true;
    }
    return false;
  }

  @NotNull
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

  @NotNull
  @Override
  public List<CommitHistoryTreeListItem> tryReadEntries(int pNumEntries)
  {
    List<CommitHistoryTreeListItem> entries = new ArrayList<>(pNumEntries);
    for (int index = 0; index < pNumEntries && hasNext(); index++)
    {
      entries.add(next());
    }
    return entries;
  }

  @NotNull
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
          else if (formerLine.getLineType() != AncestryLine.LineType.STILLBORN && formerLine.getLineType() != AncestryLine.LineType.EMPTY)
          {
            newLines.add(formerLine);
          }
        }
      }
      if (isBranchHead)
      {
        advancedLine = _getBranchHeads(newLines, parentLines);
      }
    }
    else
    {
      // start from scratch and create AncestryLine from the very first commmit
      advancedLine = _getBranchHeads(newLines, parentLines);
    }
    // check for stillborns
    if (advancedLine != null && parentLines.size() > 1)
    {
      _checkForStillborn(pBufferdCommit, newLines, parentLines, advancedLine);
    }

    HistoryGraphElement historyGraphElement = new HistoryGraphElement();
    historyGraphElement.calculateUpperLines(latestHistoryItem == null ? List.of() : latestHistoryItem.getAncestryLines(), advancedLine, currentCommit);
    historyGraphElement.calculateLowerLines(newLines);
    return new CommitHistoryTreeListItem(currentCommit, newLines, historyGraphElement, allBranches, allTags);
  }

  /**
   * Note: the returned advanced line is the first parent line here, since this is branchhead and we're starting a new line (or lines if parents > 1)
   *
   * @param pNewLines    list of the ancestryLines as they are after the current commit
   * @param pParentLines list of lines that start at the current commit
   * @return the advanced line
   */
  @NotNull
  private AncestryLine _getBranchHeads(@NotNull List<AncestryLine> pNewLines, @NotNull List<AncestryLine> pParentLines)
  {
    if (currentCommit.getParents().isEmpty())
    {
      pParentLines.add(new AncestryLine(currentCommit, colorRoulette.get(), AncestryLine.LineType.EMPTY));
    }
    else
    {
      pParentLines.add(new AncestryLine(currentCommit.getParents().get(0), colorRoulette.get(), AncestryLine.LineType.FULL));
      for (int index = 1; index < currentCommit.getParents().size(); index++)
      {
        pParentLines.add(new AncestryLine(currentCommit.getParents().get(index), colorRoulette.get(), AncestryLine.LineType.INFANT));
      }
    }
    pNewLines.addAll(pParentLines);
    return pParentLines.get(0);
  }

  /**
   * checks if any of the parent lines is stillborn
   *
   * @param pBufferdCommit next commit in the list
   * @param pNewLines      List of AncestryLines after the current commit
   * @param pParentLines   List of AncestryLines that "spawned" from the knot of the current commit
   * @param pAdvancedLine  the AncestryLine that lead to the knot of the current commit
   */
  private void _checkForStillborn(@NotNull ICommit pBufferdCommit, @NotNull List<AncestryLine> pNewLines, @NotNull List<AncestryLine> pParentLines,
                                  @NotNull AncestryLine pAdvancedLine)
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
          double stillBornMeetingIndex = _getStillbornMeetingOffset(fullLineCount, pAdvancedLine, pNewLines);
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
   * @param pNewLines     the current lines, in case the advanced line is not in the ancestryLines of the latestHistoryItem (aka a branchhead with > 1 parents)
   * @return index at which the line for the stillborn intersects the bootom of the table cell
   */
  private double _getStillbornMeetingOffset(int pIndex, @NotNull AncestryLine pAdvancedLine, @NotNull List<AncestryLine> pNewLines)
  {
    List<AncestryLine> linesToAnalyze;
    if (latestHistoryItem.getAncestryLines().contains(pAdvancedLine))
      linesToAnalyze = latestHistoryItem.getAncestryLines().subList(0, latestHistoryItem.getAncestryLines().indexOf(pAdvancedLine));
    else
      linesToAnalyze = pNewLines.subList(0, pNewLines.indexOf(pAdvancedLine));
    return (double) (pIndex + linesToAnalyze
        .stream()
        .filter(pLine -> pLine.getLineType() == AncestryLine.LineType.FULL)
        .count())
        / 2;
  }

  /**
   * @param pParentLineColor Color that the AncestryLine leading to the current commit had
   * @return AncestryLines that spawn from the current commit
   */
  @NotNull
  private List<AncestryLine> _getParentLines(@NotNull Color pParentLineColor)
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
    else
    {
      parentLines.add(new AncestryLine(currentCommit, pParentLineColor, AncestryLine.LineType.EMPTY));
    }
    return parentLines;
  }

}
