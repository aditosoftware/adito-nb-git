package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author m.kaspera, 14.06.2022
 */
public class WordBasedResolveOption implements ResolveOption
{
  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NotNull IChangeDelta acceptedDelta, @NotNull IFileDiff pAcceptedDiff, @NotNull IFileDiff pOtherDiff, @NotNull EConflictSide pConflictSide, @NotNull ConflictPair pConflictPair)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = pAcceptedDiff.acceptDelta(acceptedDelta, true, true, false);
    deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> pOtherDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                       pDeltaTextChangeEvent.getLength(),
                                                                                       pDeltaTextChangeEvent.getText(), EChangeSide.OLD, false, false));
    return List.of();
  }

  @Override
  public boolean canResolveConflict(@NotNull IChangeDelta pChangeDelta, @NotNull IChangeDelta pOtherDelta, @NotNull EConflictSide pConflictSide,
                                    @NotNull IFileDiffHeader pFileDiffHeader)
  {
    return pOtherDelta.getLinePartChanges()
        .stream().allMatch(pLinePartChangeDelta -> pChangeDelta.getLinePartChanges()
            .stream().noneMatch(pOwnLinePartChangeDelta -> pOwnLinePartChangeDelta.isConflictingWith(pLinePartChangeDelta)));
  }

  @Override
  public int getPosition()
  {
    return 300;
  }
}
