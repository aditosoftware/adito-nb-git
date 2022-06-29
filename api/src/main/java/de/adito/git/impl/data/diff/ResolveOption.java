package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author m.kaspera, 13.06.2022
 */
public interface ResolveOption
{

  List<IDeltaTextChangeEvent> resolveConflict(@NotNull IChangeDelta acceptedDelta, @NotNull IFileDiff pAcceptedDiff, @NotNull IFileDiff pOtherDiff, EConflictSide pConflictSide, ConflictPair pConflictPair);

  boolean canResolveConflict(@NotNull IChangeDelta pChangeDelta, @NotNull IChangeDelta pOtherDelta, @NotNull EConflictSide pConflictSide);

  /**
   * Determines which ResolveOptions are applied first
   *
   * @return position that the ResolveOption should have, a lower number means the ResolveOption is applied first
   */
  int getPosition();

}
