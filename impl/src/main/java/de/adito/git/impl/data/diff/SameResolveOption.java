package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author m.kaspera, 14.06.2022
 */
public class SameResolveOption implements ResolveOption
{
  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NotNull IChangeDelta acceptedDelta, @NotNull IFileDiff pAcceptedDiff, @NotNull IFileDiff pOtherDiff, @NotNull EConflictSide pConflictSide, @NotNull ConflictPair pConflictPair)
  {
    pAcceptedDiff.acceptDelta(acceptedDelta, false, true, false);
    pOtherDiff.acceptDelta(pOtherDiff.getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.getOpposite(pConflictSide))), false, false, false);
    // no deltaTextChangeEvent here because the other side has the change applied "manually" in the line above
    return List.of();
  }

  @Override
  public boolean canResolveConflict(@NotNull IChangeDelta pChangeDelta, @NotNull IChangeDelta pOtherDelta, @NotNull EConflictSide pConflictSide,
                                    @NotNull IFileDiffHeader pFileDiffHeader)
  {
    return pChangeDelta.getStartTextIndex(EChangeSide.OLD) == pOtherDelta.getStartTextIndex(EChangeSide.OLD)
        && pChangeDelta.getEndTextIndex(EChangeSide.OLD) == pOtherDelta.getEndTextIndex(EChangeSide.OLD)
        && pChangeDelta.getText(EChangeSide.NEW).equals(pOtherDelta.getText(EChangeSide.NEW));
  }

  @Override
  public int getPosition()
  {
    return 0;
  }
}
