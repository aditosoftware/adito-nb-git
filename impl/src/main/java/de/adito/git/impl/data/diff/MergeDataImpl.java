package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author m.kaspera, 06.03.2020
 */
public class MergeDataImpl implements IMergeData
{

  private final IFileDiff yourSideDiff;
  private final IFileDiff theirSideDiff;

  public MergeDataImpl(IFileDiff pYourSideDiff, IFileDiff pTheirSideDiff)
  {
    yourSideDiff = pYourSideDiff;
    theirSideDiff = pTheirSideDiff;
  }

  @Override
  public String getFilePath()
  {
    if (yourSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME && theirSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME)
      return yourSideDiff.getFileHeader().getFilePath();
    else if (yourSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME)
    {
      return yourSideDiff.getFileHeader().getFilePath();
    }
    else if (theirSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME)
    {
      return theirSideDiff.getFileHeader().getFilePath();
    }
    else
    {
      if (theirSideDiff.getFile(EChangeSide.OLD).equals(yourSideDiff.getFile(EChangeSide.OLD)))
        return theirSideDiff.getFileHeader().getFilePath(EChangeSide.OLD);
      else return theirSideDiff.getFileHeader().getFilePath(EChangeSide.NEW);
    }
  }

  @NotNull
  @Override
  public IFileDiff getDiff(@NotNull EConflictSide conflictSide)
  {
    return conflictSide == EConflictSide.YOURS ? yourSideDiff : theirSideDiff;
  }

  @Override
  public void acceptDelta(@NotNull IChangeDelta acceptedDelta, @NotNull EConflictSide conflictSide)
  {
    if (acceptedDelta.getChangeStatus().getChangeStatus() == EChangeStatus.UNDEFINED)
    {
      throw new IllegalArgumentException("Cannot accept a delta of state UNDEFINED");
    }
    List<IDeltaTextChangeEvent> deltaTextChangeEvents;
    if (conflictSide == EConflictSide.YOURS)
    {
      deltaTextChangeEvents = yourSideDiff.acceptDelta(acceptedDelta);
      deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> theirSideDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                            pDeltaTextChangeEvent.getLength(),
                                                                                            pDeltaTextChangeEvent.getText(), EChangeSide.OLD));
    }
    else
    {
      deltaTextChangeEvents = theirSideDiff.acceptDelta(acceptedDelta);
      deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> yourSideDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                           pDeltaTextChangeEvent.getLength(),
                                                                                           pDeltaTextChangeEvent.getText(), EChangeSide.OLD));
    }
  }

  @Override
  public void discardChange(@NotNull IChangeDelta discardedDelta, @NotNull EConflictSide conflictSide)
  {
    if (conflictSide == EConflictSide.YOURS)
    {
      yourSideDiff.discardDelta(discardedDelta);
      theirSideDiff.processTextEvent(0, 0, null, EChangeSide.OLD);
    }
    else
    {
      theirSideDiff.discardDelta(discardedDelta);
      yourSideDiff.processTextEvent(0, 0, null, EChangeSide.OLD);
    }
  }

  @Override
  public void reset()
  {
    yourSideDiff.reset();
    theirSideDiff.reset();
  }

  @Override
  public void modifyText(@Nullable String text, int length, int offset)
  {
    if (text == null)
    {
      yourSideDiff.processTextEvent(offset, length, null, EChangeSide.OLD);
      theirSideDiff.processTextEvent(offset, length, null, EChangeSide.OLD);
    }
    else
    {
      yourSideDiff.processTextEvent(offset, length, text, EChangeSide.OLD);
      theirSideDiff.processTextEvent(offset, length, text, EChangeSide.OLD);
    }
  }

  @Override
  public void markConflicting()
  {
    theirSideDiff.markConflicting(yourSideDiff);
    yourSideDiff.markConflicting(theirSideDiff);
  }

  /**
   * Takes the given editLists and adjusts the contained edits such that two conflicting edits reference the same lines in the A-side version
   *
   * @param pEditList      First list of edits
   * @param pOtherEditList second list of edits
   */
  public static void adjustEditListForMerge(@NotNull EditList pEditList, @NotNull EditList pOtherEditList)
  {
    for (int mainIndex = 0; mainIndex < pEditList.size(); mainIndex++)
    {
      for (int otherIndex = 0; otherIndex < pOtherEditList.size(); otherIndex++)
      {
        if (_doesOverlap(pEditList.get(mainIndex), pOtherEditList.get(otherIndex)))
        {
          pEditList.set(mainIndex, _mergeEdit(pEditList.get(mainIndex), pOtherEditList.get(otherIndex)));
          pOtherEditList.set(otherIndex, _mergeEdit(pOtherEditList.get(otherIndex), pEditList.get(mainIndex)));
        }
      }
    }
    _compressList(pEditList);
    _compressList(pOtherEditList);
  }

  /**
   * Goes throught the editList and combines adjacent or overlapping edits
   *
   * @param pEditList EditList to compress
   */
  private static void _compressList(EditList pEditList)
  {
    if (!pEditList.isEmpty())
    {
      Edit currentEdit;
      Edit nextEdit;
      for (int index = 0; index < pEditList.size() - 1; )
      {
        currentEdit = pEditList.get(index);
        nextEdit = pEditList.get(index + 1);
        if (currentEdit.getEndA() >= nextEdit.getBeginA() || currentEdit.getEndB() >= nextEdit.getBeginB())
        {
          pEditList.set(index, new Edit(currentEdit.getBeginA(),
                                        nextEdit.getEndA(),
                                        currentEdit.getBeginB(),
                                        nextEdit.getEndB()));
          pEditList.remove(index + 1);
        }
        else
          index++;
      }
    }
  }

  /**
   * "Merges" pEdit with pOtherEdit based on their A-side lines. The A-Side lines for the returned edit and pOtherEdit will be the same,
   * with the B-side lines of pEdit adjusted such that any additional lines taken in on the A-Side are reflected on the B-side
   *
   * @param pEdit      Edit that should form the basis of the B-side lines, this is the "main" edit
   * @param pOtherEdit This edit should be combined with the first
   * @return Edit
   */
  private static Edit _mergeEdit(Edit pEdit, Edit pOtherEdit)
  {
    int startOffsetB = Math.min(pEdit.getBeginA(), pOtherEdit.getBeginA()) - pEdit.getBeginA();
    int endOffsetB = Math.max(pEdit.getEndA(), pOtherEdit.getEndA()) - pEdit.getEndA();
    return new Edit(Math.min(pEdit.getBeginA(), pOtherEdit.getBeginA()), Math.max(pEdit.getEndA(), pOtherEdit.getEndA()),
                    pEdit.getBeginB() + startOffsetB, pEdit.getEndB() + endOffsetB);
  }

  /**
   * Checks if the given edits overlap on their A-side
   *
   * @param pEdit      first edit
   * @param pOtherEdit second edit
   * @return false if the edits do not overlap or are the same (on the A-side), true otherwise
   */
  private static boolean _doesOverlap(Edit pEdit, Edit pOtherEdit)
  {
    if (pOtherEdit.getEndA() == pEdit.getEndA() && pOtherEdit.getBeginA() == pEdit.getBeginA())
      return false;
    if (pOtherEdit.getEndA() < pEdit.getBeginA())
      return false;
    if (pOtherEdit.getEndA() <= pEdit.getEndA())
      return true;
    return pOtherEdit.getBeginA() <= pEdit.getEndA();
  }
}
