package de.adito.git.api.data.diff;


import de.adito.git.impl.data.diff.ConflictPair;
import de.adito.git.impl.data.diff.ResolveOptionsProvider;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface defining the methods required for a data structure that handles information about a merge
 *
 * @author m.kaspera, 06.03.2020
 */
public interface IMergeData
{

  /**
   * Determines the name of the file for which this is the diff. If one or two of the sides are renames, the name is determined in the following way:
   * - If three side/version (e.g. YOURS.old, YOURS.new, THEIRS.old) are in agreement, take that file name
   * - If only two side/versions are in agreement, one version has to be in agreement. Take that version. (e.g. YOURS.old and THEIRS.old agree, YOURS.new is file b and
   * THEIRS.new is file c. It is not possible that YOURS.old and THEIRS.new agree, and YOUR.new is file b and THEIRS.old is file c)
   *
   * @return name of the file
   */
  String getFilePath();

  /**
   * get the IFileDiff of the specified conflict side
   *
   * @param conflictSide CONFLICT_SIDE describing if the diff from base-side or branch-to-merge-side to fork-point is wanted
   * @return IFileDiff for the comparison branch-to-merge to fork-point commit (CONFLICT_SIDE.THEIRS) or base-side to fork-point commit (CONFLICT_SIDE.YOURS)
   */
  @NonNull
  IFileDiff getDiff(@NonNull EConflictSide conflictSide);

  /**
   * accepts all changes from the given chunk and applies these changes to the other conflict side
   *
   * @param acceptedDelta the change that should be accepted and added to the fork-point commit
   * @param conflictSide  CONFLICT_SIDE from which the chunk originates
   */
  void acceptDelta(@NonNull IChangeDelta acceptedDelta, @NonNull EConflictSide conflictSide);

  /**
   * @param pDelta        Delta whose ConflictPair should be calculated
   * @param pFileDiff     FileDiff that pDelta is part of
   * @param pConflictSide The side of the FileDiff that pDelta originates from
   * @return The ConflictPair that pDelta is part of, or null if pDelta is not part of a conflict
   */
  @Nullable
  ConflictPair getConflictPair(@NonNull IChangeDelta pDelta, @NonNull IFileDiff pFileDiff, @NonNull EConflictSide pConflictSide);

  /**
   * discards the specified changes by the given chunk
   *
   * @param discardedDelta the change that should be discarded
   * @param conflictSide   CONFLICT_SIDE from which the chunk originates
   */
  void discardChange(@NonNull IChangeDelta discardedDelta, @NonNull EConflictSide conflictSide);

  /**
   * resets all Data of this object to the initial state
   */
  void reset();

  /**
   * inserts/deletes/modifies text in the fork-point commit text
   *
   * @param text   the text that was inserted, null string if remove operation
   * @param length the number of characters that were removed. 0 for an insert
   * @param offset the offset of the place of insertion from the beginning of the document
   */
  void modifyText(@Nullable String text, int length, int offset);

  /**
   * goes through the changes and marks all changes that conflict with a change from the other side as conflicting
   */
  void markConflicting(@NonNull ResolveOptionsProvider pResolveOptionsProvider);

}
