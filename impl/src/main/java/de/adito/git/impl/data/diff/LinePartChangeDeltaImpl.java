package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.ILinePartChangeDelta;
import lombok.NonNull;

import java.awt.Color;
import java.util.function.Function;

/**
 * @author m.kaspera, 02.03.2020
 */
public final class LinePartChangeDeltaImpl implements ILinePartChangeDelta
{

  private final EChangeType changeType;
  private final int startTextIndexOld;
  private final int endTextIndexOld;
  private final int startTextIndexNew;
  private final int endTextIndexNew;

  /**
   * @param pChangeType             Type of change
   * @param pChangeDeltaTextOffsets ChangeDeltaTextOffsets data object, should contain start- and endIndices of each the the two text versions,
   *                                as seen on the whole document
   */
  public LinePartChangeDeltaImpl(@NonNull EChangeType pChangeType, @NonNull ChangeDeltaTextOffsets pChangeDeltaTextOffsets)
  {
    changeType = pChangeType;
    startTextIndexOld = pChangeDeltaTextOffsets.getStartIndexOriginal();
    endTextIndexOld = pChangeDeltaTextOffsets.getEndIndexOriginal();
    startTextIndexNew = pChangeDeltaTextOffsets.getStartIndexChanged();
    endTextIndexNew = pChangeDeltaTextOffsets.getEndIndexChanged();
  }

  @Override
  @NonNull
  public EChangeType getChangeType()
  {
    return changeType;
  }

  @Override
  @NonNull
  public EConflictType getConflictType()
  {
    return EConflictType.NONE;
  }

  @Override
  public int getStartTextIndex(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? startTextIndexNew : startTextIndexOld;
  }

  @Override
  public int getEndTextIndex(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? endTextIndexNew : endTextIndexOld;
  }

  @Override
  @NonNull
  public Color getDiffColor()
  {
    return changeType.getDiffColor();
  }

  @Override
  @NonNull
  public Color getSecondaryDiffColor()
  {
    return changeType.getSecondaryDiffColor();
  }

  @Override
  public ILinePartChangeDelta applyOffset(int pTextOffset, EChangeSide pChangeSide)
  {
    int textOffsetOld = pChangeSide == EChangeSide.OLD ? pTextOffset : 0;
    int textOffsetNew = pChangeSide == EChangeSide.NEW ? pTextOffset : 0;
    return new LinePartChangeDeltaImpl(changeType, new ChangeDeltaTextOffsets(startTextIndexOld + textOffsetOld, endTextIndexOld + textOffsetOld,
                                                                              startTextIndexNew + textOffsetNew, endTextIndexNew + textOffsetNew));
  }

  @Override
  public boolean isConflictingWith(ILinePartChangeDelta pLinePartChangeDelta)
  {
    return (pLinePartChangeDelta.getStartTextIndex(EChangeSide.OLD) < endTextIndexOld && pLinePartChangeDelta.getEndTextIndex(EChangeSide.OLD) > startTextIndexOld)
        || pLinePartChangeDelta.getStartTextIndex(EChangeSide.OLD) == endTextIndexOld && pLinePartChangeDelta.getEndTextIndex(EChangeSide.OLD) == startTextIndexOld;
  }

  @Override
  public ILinePartChangeDelta processTextEvent(int pOffset, int pLength, boolean pIsInsert, EChangeSide pChangeSide)
  {
    int startTextIndex = pChangeSide == EChangeSide.NEW ? startTextIndexNew : startTextIndexOld;
    int endTextIndex = pChangeSide == EChangeSide.NEW ? endTextIndexNew : endTextIndexOld;
    ChangeDeltaTextOffsets modifiedChangeDeltaOffsets;
    if (pIsInsert)
    {
      modifiedChangeDeltaOffsets = updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex, pEndTextIndex -> pEndTextIndex + pLength);
    }
    else
    {
      if (pOffset < startTextIndex)
      {
        if (pOffset + -pLength < endTextIndex)
        {
          // case DELETE 1
          modifiedChangeDeltaOffsets = updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex + (pOffset - pStartTextIndex),
                                                         pEndTextIndex -> pEndTextIndex + pLength);
        }
        else
        {
          // case DELETE 2
          modifiedChangeDeltaOffsets = updateTextIndizes(pChangeSide, pStartTextIndex -> pOffset, pEndTextIndex -> pOffset);
        }
      }
      else
      {
        if (pOffset + -pLength <= endTextIndex)
        {
          // case DELETE 3/7
          modifiedChangeDeltaOffsets = updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex, pEndTextIndex -> pEndTextIndex + pLength);
        }
        else
        {
          // case DELETE 4
          modifiedChangeDeltaOffsets = updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex, pEndTextIndex -> pEndTextIndex - (pEndTextIndex - pOffset));
        }
      }
    }
    return new LinePartChangeDeltaImpl(changeType, modifiedChangeDeltaOffsets);
  }

  /**
   * Applies the given Functions to the start- and endTextIndices of the specified side and returns the result as ChangeDeltaTextOffsets
   *
   * @param pChangeSide           Which side should have its indices updated
   * @param pStartTextIndexUpdate Function to apply to the startIndex
   * @param pEndTextIndexUpdate   Function to apply to the endIndex
   * @return ChangeDeltaTextOffsets with updated indices
   */
  private ChangeDeltaTextOffsets updateTextIndizes(EChangeSide pChangeSide, Function<Integer, Integer> pStartTextIndexUpdate,
                                                   Function<Integer, Integer> pEndTextIndexUpdate)
  {
    if (pChangeSide == EChangeSide.NEW)
      return new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, pStartTextIndexUpdate.apply(startTextIndexNew), pEndTextIndexUpdate.apply(endTextIndexNew));
    return new ChangeDeltaTextOffsets(pStartTextIndexUpdate.apply(startTextIndexOld), pEndTextIndexUpdate.apply(endTextIndexOld), startTextIndexNew, endTextIndexNew);
  }
}
