package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author m.kaspera, 14.06.2022
 */
public class EnclosedResolveOption implements ResolveOption
{

  @Nullable
  private EConflictType conflictType;

  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NonNull IChangeDelta acceptedDelta, @NonNull IFileDiff pAcceptedDiff, @NonNull IFileDiff pOtherDiff, @NonNull EConflictSide pConflictSide, @NonNull ConflictPair pConflictPair)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = null;
    AtomicBoolean isTryToSnapToDelta = new AtomicBoolean(false);
    EConflictType determinedConflictType = determineConflictType(acceptedDelta,
                                                                 pOtherDiff.getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.getOpposite(pConflictSide))),
                                                                 pConflictSide);
    Runnable executeAfterTextUpdates = () -> {
    };
    if (_enclosesAcceptedOther(pConflictPair, pOtherDiff, pConflictSide, determinedConflictType))
    {
      deltaTextChangeEvents = pAcceptedDiff.acceptDelta(acceptedDelta, false, true, true);
    }
    else if (_enclosedByOther(pConflictPair, pOtherDiff, pConflictSide, determinedConflictType))
    {
      deltaTextChangeEvents = pAcceptedDiff.acceptDelta(acceptedDelta, false, true, false);
      isTryToSnapToDelta.set(true);
    }
    else if (_isEnclosing(pConflictSide, determinedConflictType))
    {
      deltaTextChangeEvents = pAcceptedDiff.acceptDelta(acceptedDelta, false, true, false);
      // if the other change is not yet accepted, but is enclosed by this (accepted) change, then the other change should also count as accepted
      // (since all changes of the other were implicitly accepted as well)
      // setting the accepted state and the text event do have to happen after processing the textChangeEvent that was triggered by accepting this change
      executeAfterTextUpdates = () -> {
        IChangeDelta otherChangeDelta = pOtherDiff.getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.getOpposite(pConflictSide)));
        IChangeDelta changedChangeDelta = otherChangeDelta.setChangeStatus(new ChangeStatusImpl(EChangeStatus.ACCEPTED, otherChangeDelta.getChangeType(), otherChangeDelta.getConflictType()));
        pOtherDiff.getChangeDeltas().set(pConflictPair.getIndexOfSide(EConflictSide.getOpposite(pConflictSide)), changedChangeDelta);
        pOtherDiff.processTextEvent(0, 0, "", EChangeSide.NEW, false, false);
      };

    }
    if (deltaTextChangeEvents != null)
    {
      deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> pOtherDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                         pDeltaTextChangeEvent.getLength(),
                                                                                         pDeltaTextChangeEvent.getText(), EChangeSide.OLD, isTryToSnapToDelta.get(), false));
      executeAfterTextUpdates.run();
    }
    return List.of();
  }

  @Override
  public boolean canResolveConflict(@NonNull IChangeDelta pChangeDelta, @NonNull IChangeDelta pOtherDelta, @NonNull EConflictSide pConflictSide,
                                    @NonNull IFileDiffHeader pFileDiffHeader)
  {
    return determineConflictType(pChangeDelta, pOtherDelta, pConflictSide) != null;
  }

  @Override
  public int getPosition()
  {
    return 100;
  }

  @Nullable
  public EConflictType getConflictType()
  {
    return conflictType;
  }

  @Nullable
  private EConflictType determineConflictType(@NonNull IChangeDelta pChangeDelta, @NonNull IChangeDelta pOtherDelta, @NonNull EConflictSide pConflictSide)
  {
    conflictType = null;
    if (checkEnclosed(pChangeDelta, pOtherDelta, EConflictSide.YOURS))
    {
      if (pConflictSide == EConflictSide.THEIRS)
        conflictType = EConflictType.ENCLOSED_BY_THEIRS;
      else
        conflictType = EConflictType.ENCLOSED_BY_YOURS;
    }
    else if (checkEnclosed(pChangeDelta, pOtherDelta, EConflictSide.THEIRS))
    {
      if (pConflictSide == EConflictSide.THEIRS)
        conflictType = EConflictType.ENCLOSED_BY_YOURS;
      else
        conflictType = EConflictType.ENCLOSED_BY_THEIRS;
    }
    return conflictType;
  }

  /**
   * checks if this change is enclosed by the other, conflicting change and checks if that other change is still pending
   *
   * @param pConflictPair           EConflictType giving the ENCLOSED_BY_XXX type
   * @param pOtherDiff              the other, conflicting IFileDiff
   * @param pConflictSide           conflict side of this change
   * @param pDeterminedConflictType
   * @return true if this change is enclosed by the other, conflicting change and the other change is still pending and not accepted
   */
  private boolean _enclosedByOther(ConflictPair pConflictPair, IFileDiff pOtherDiff, EConflictSide pConflictSide, EConflictType pDeterminedConflictType)
  {
    return !_isCounterPartAccepted(pConflictPair, pOtherDiff, pConflictSide) && _isEnclosed(pConflictSide, pDeterminedConflictType);
  }

  /**
   * checks if the change encloses the other, conflicting change and checks if that other change is already accepted
   *
   * @param pConflictPair           EConflictType giving the ENCLOSED_BY_XXX type
   * @param pOtherDiff              the other, conflicting IFileDiff
   * @param pConflictSide           conflict side of this change
   * @param pDeterminedConflictType
   * @return true if this change encloses the other, conflicting change and the other change is also already accepted
   */
  private boolean _enclosesAcceptedOther(ConflictPair pConflictPair, @NonNull IFileDiff pOtherDiff, EConflictSide pConflictSide, EConflictType pDeterminedConflictType)
  {
    return _isCounterPartAccepted(pConflictPair, pOtherDiff, pConflictSide) && _isEnclosing(pConflictSide, pDeterminedConflictType);
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

  /**
   * true if this change encloses the other change (this change contains all changes included in the other change and more)
   *
   * @param pConflictSide conflict side of this change
   * @return true if this change encloses the other (conflicting) change
   */
  private boolean _isEnclosing(EConflictSide pConflictSide, EConflictType pDeterminedConflictType)
  {
    return (pDeterminedConflictType == EConflictType.ENCLOSED_BY_THEIRS && pConflictSide == EConflictSide.THEIRS)
        || (pDeterminedConflictType == EConflictType.ENCLOSED_BY_YOURS && pConflictSide == EConflictSide.YOURS);
  }

  /**
   * true if this change is enclosed by the other change (the other change contains all changes included in this one and more)
   *
   * @param pConflictSide conflict side of this change
   * @return true if this change is enclosed by the other (conflicting) change
   */
  private boolean _isEnclosed(EConflictSide pConflictSide, EConflictType pDeterminedConflictType)
  {
    return (pDeterminedConflictType == EConflictType.ENCLOSED_BY_THEIRS && pConflictSide == EConflictSide.YOURS)
        || (pDeterminedConflictType == EConflictType.ENCLOSED_BY_YOURS && pConflictSide == EConflictSide.THEIRS);
  }

  /**
   * @param pChangeDelta      a ChangeDelta
   * @param pOtherChangeDelta the other ChangeDelta
   * @param pConflictSide     YOURS here means "check if this text contains the text of pOtherChangeDelta", the opposite for THEIRS
   * @return true if the text of the requested ChangeDelta contains the text of the other ChangeDelta
   */
  private boolean checkEnclosed(@NonNull IChangeDelta pChangeDelta, @NonNull IChangeDelta pOtherChangeDelta, @NonNull EConflictSide pConflictSide)
  {
    if (pOtherChangeDelta.getChangeType() == EChangeType.DELETE || pChangeDelta.getChangeType() == EChangeType.DELETE)
      return false;
    if (pConflictSide == EConflictSide.YOURS)
    {
      return pChangeDelta.getText(EChangeSide.NEW).contains(pOtherChangeDelta.getText(EChangeSide.NEW));
    }
    else
    {
      return pOtherChangeDelta.getText(EChangeSide.NEW).contains(pChangeDelta.getText(EChangeSide.NEW));
    }
  }
}
