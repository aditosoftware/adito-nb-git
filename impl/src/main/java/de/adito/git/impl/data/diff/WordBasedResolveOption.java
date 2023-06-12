package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import lombok.NonNull;

import java.util.List;

/**
 * @author m.kaspera, 14.06.2022
 */
public class WordBasedResolveOption implements ResolveOption
{
  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NonNull IChangeDelta acceptedDelta, @NonNull IFileDiff pAcceptedDiff, @NonNull IFileDiff pOtherDiff, @NonNull EConflictSide pConflictSide, @NonNull ConflictPair pConflictPair)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = pAcceptedDiff.acceptDelta(acceptedDelta, true, true, false);
    deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> pOtherDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                       pDeltaTextChangeEvent.getLength(),
                                                                                       pDeltaTextChangeEvent.getText(), EChangeSide.OLD, false, false));
    return List.of();
  }

  @Override
  public boolean canResolveConflict(@NonNull IChangeDelta pChangeDelta, @NonNull IChangeDelta pOtherDelta, @NonNull EConflictSide pConflictSide,
                                    @NonNull IFileDiffHeader pFileDiffHeader)
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
