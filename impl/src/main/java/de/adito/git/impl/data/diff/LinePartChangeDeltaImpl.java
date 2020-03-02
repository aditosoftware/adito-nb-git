package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.ILinePartChangeDelta;
import org.jetbrains.annotations.NotNull;

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
  public LinePartChangeDeltaImpl(@NotNull EChangeType pChangeType, @NotNull ChangeDeltaTextOffsets pChangeDeltaTextOffsets)
  {
    changeType = pChangeType;
    startTextIndexOld = pChangeDeltaTextOffsets.getStartIndexOriginal();
    endTextIndexOld = pChangeDeltaTextOffsets.getEndIndexOriginal();
    startTextIndexNew = pChangeDeltaTextOffsets.getStartIndexChanged();
    endTextIndexNew = pChangeDeltaTextOffsets.getEndIndexChanged();
  }

  @Override
  public EChangeType getChangeType()
  {
    return changeType;
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
  public ILinePartChangeDelta applyOffset(int pTextOffset)
  {
    return new LinePartChangeDeltaImpl(changeType, new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew));
  }
}
