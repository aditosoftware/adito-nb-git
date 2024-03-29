package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import lombok.NonNull;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author m.kaspera, 06.03.2020
 */
public class MergeDataImpl implements IMergeData
{

  private final IFileDiff yourSideDiff;
  private final IFileDiff theirSideDiff;
  private List<ConflictPair> conflictPairs;

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

  @NonNull
  @Override
  public IFileDiff getDiff(@NonNull EConflictSide conflictSide)
  {
    return conflictSide == EConflictSide.YOURS ? yourSideDiff : theirSideDiff;
  }

  @Override
  public void acceptDelta(@NonNull IChangeDelta acceptedDelta, @NonNull EConflictSide conflictSide)
  {
    if (acceptedDelta.getChangeStatus() == EChangeStatus.UNDEFINED)
    {
      throw new IllegalArgumentException("Cannot accept a delta of state UNDEFINED");
    }
    if (conflictSide == EConflictSide.YOURS)
    {
      _acceptDelta(acceptedDelta, yourSideDiff, theirSideDiff, conflictSide);
    }
    else
    {
      _acceptDelta(acceptedDelta, theirSideDiff, yourSideDiff, conflictSide);
    }
  }

  /**
   * Accepts the given delta and applies the returned IDeltaTextChangeEvent to pOtherDiff
   *
   * @param acceptedDelta Delta to accept
   * @param pAcceptedDiff IFileDiff that contains the Delta to accept
   * @param pOtherDiff    IFileDiff that doesn't contain the Delta to accept, has to also change due to the effects of the applied delta
   * @param pConflictSide CONFLICT_SIDE from which the accepted chunk originates
   */
  private void _acceptDelta(@NonNull IChangeDelta acceptedDelta, @NonNull IFileDiff pAcceptedDiff, @NonNull IFileDiff pOtherDiff, EConflictSide pConflictSide)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents;
    AtomicBoolean trySnapToDelta = new AtomicBoolean(false);
    Optional<ConflictPair> conflictPairOpt = Optional.ofNullable(getConflictPair(acceptedDelta, pAcceptedDiff, pConflictSide));
    deltaTextChangeEvents = conflictPairOpt.map(ConflictPair::getType)
        .map(ConflictType::getResolveOption)
        .map(pResolveOption -> pResolveOption.resolveConflict(acceptedDelta, pAcceptedDiff, pOtherDiff, pConflictSide, conflictPairOpt.get())).orElse(null);
    if (deltaTextChangeEvents == null)
    {
      if (conflictPairOpt.isPresent() && conflictPairOpt.get().getType().getConflictType() == EConflictType.CONFLICTING &&
          _isCounterPartAccepted(conflictPairOpt.get(), pOtherDiff, pConflictSide))
      {
        deltaTextChangeEvents = List.of(pAcceptedDiff.appendDeltaText(acceptedDelta));
        trySnapToDelta.getAndSet(true);
      }
      else
      {
        deltaTextChangeEvents = pAcceptedDiff.acceptDelta(acceptedDelta, false, true, false);
      }
    }
    deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> pOtherDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                       pDeltaTextChangeEvent.getLength(),
                                                                                       pDeltaTextChangeEvent.getText(), EChangeSide.OLD, trySnapToDelta.get(), false));
  }

  /**
   * checks if the other part of the conflictPair is accepted
   *
   * @param pConflictPair ConflictPair giving the index of the chunk to check
   * @param pOtherDiff    Opposite side of pAcceptedDiff
   * @param pConflictSide Side of the conflict that has the accepted delta
   * @return true if the changeDelta is part of a conflictPair and the other side has status ACCEPTED, false otherwise
   */
  private boolean _isCounterPartAccepted(@NonNull ConflictPair pConflictPair, @NonNull IFileDiff pOtherDiff,
                                         EConflictSide pConflictSide)
  {
    return pOtherDiff.getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.getOpposite(pConflictSide)))
        .getChangeStatus() == EChangeStatus.ACCEPTED;

  }

  @Override
  @Nullable
  public ConflictPair getConflictPair(@NonNull IChangeDelta pDelta, @NonNull IFileDiff pFileDiff, @NonNull EConflictSide pConflictSide)
  {
    int deltaIndex = pFileDiff.getChangeDeltas().indexOf(pDelta);
    if (conflictPairs != null)
    {
      return conflictPairs.stream().filter(pConflictPair -> pConflictPair.getIndexOfSide(pConflictSide) == deltaIndex).findFirst().orElse(null);
    }
    return null;
  }

  @Override
  public void discardChange(@NonNull IChangeDelta discardedDelta, @NonNull EConflictSide conflictSide)
  {
    if (conflictSide == EConflictSide.YOURS)
    {
      yourSideDiff.discardDelta(discardedDelta);
      theirSideDiff.processTextEvent(0, 0, null, EChangeSide.OLD, false, false);
      theirSideDiff.processTextEvent(0, 0, null, EChangeSide.NEW, false, false);
    }
    else
    {
      theirSideDiff.discardDelta(discardedDelta);
      yourSideDiff.processTextEvent(0, 0, null, EChangeSide.OLD, false, false);
      yourSideDiff.processTextEvent(0, 0, null, EChangeSide.NEW, false, false);
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
      yourSideDiff.processTextEvent(offset, length, null, EChangeSide.OLD, false, false);
      theirSideDiff.processTextEvent(offset, length, null, EChangeSide.OLD, false, false);
    }
    else
    {
      yourSideDiff.processTextEvent(offset, length, text, EChangeSide.OLD, false, false);
      theirSideDiff.processTextEvent(offset, length, text, EChangeSide.OLD, false, false);
    }
  }

  @Override
  public void markConflicting(@NonNull ResolveOptionsProvider pResolveOptionsProvider)
  {
    theirSideDiff.markConflicting(yourSideDiff, EConflictSide.YOURS, pResolveOptionsProvider);
    conflictPairs = yourSideDiff.markConflicting(theirSideDiff, EConflictSide.THEIRS, pResolveOptionsProvider);
  }

  /**
   * Takes the given editLists and adjusts the contained edits such that two conflicting edits reference the same lines in the A-side version
   *
   * @param pEditList      First list of edits
   * @param pOtherEditList second list of edits
   */
  public static void adjustEditListForMerge(@NonNull EditList pEditList, @NonNull EditList pOtherEditList)
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
      // the loop counter is not updated on each loop, but only if the edit was not changed -> this way, several adjacent edits may be combined/folded into one with
      // only a single loop
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

  @Override
  public String toString()
  {
    return getFilePath();
  }
}
