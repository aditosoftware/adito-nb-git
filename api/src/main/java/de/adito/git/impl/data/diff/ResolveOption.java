package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author m.kaspera, 13.06.2022
 */
public interface ResolveOption
{

  /**
   * @param acceptedDelta conflicting delta that should be accepted (the other conflicting delta should be accepted or discarded as well)
   * @param pAcceptedDiff IFileDiff, that contains acceptedDelta
   * @param pOtherDiff    IFileDiff, that contains the second side of the conflict
   * @param pConflictSide which side acceptedDelta is from
   * @param pConflictPair ConflictPair with the information about which indices the conflicting deltas have in each FileDiff
   * @return List of TextChangeEvents that have to be performed to resolve the conflict
   */
  List<IDeltaTextChangeEvent> resolveConflict(@NotNull IChangeDelta acceptedDelta, @NotNull IFileDiff pAcceptedDiff, @NotNull IFileDiff pOtherDiff,
                                              @NotNull EConflictSide pConflictSide, @NotNull ConflictPair pConflictPair);

  /**
   * @param pChangeDelta    conflicting delta
   * @param pOtherDelta     conflicting delta
   * @param pConflictSide   which side pChangeDelta is from
   * @param pFileDiffHeader IFileDiffHeader containing information about encoding and which kind of file the conflict originates from (xml, js...)
   * @return true if the ResolveOption can resolve the conflict
   */
  boolean canResolveConflict(@NotNull IChangeDelta pChangeDelta, @NotNull IChangeDelta pOtherDelta, @NotNull EConflictSide pConflictSide,
                             @NotNull IFileDiffHeader pFileDiffHeader);

  /**
   * Determines which ResolveOptions are applied first
   *
   * @return position that the ResolveOption should have, a lower number means the ResolveOption is applied first
   */
  int getPosition();

}
