package de.adito.git.impl.data.diff;

import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a Factory that creates a type of ChangeDelta from an Edit and a ChangeDeltaTextOffsets data object
 *
 * @author m.kaspera, 27.02.2020
 */
interface IChangeDeltaFactory<TYPE>
{

  /**
   * @param pEdit             Edit for the ChangeDelta, can be used for Type and line info
   * @param pDeltaTextOffsets contain the start- and endoffsets for the text indices of the chunk in original and changed version
   * @return Object of type TYPE
   */
  @NotNull
  TYPE createDelta(@NotNull Edit pEdit, @NotNull ChangeDeltaTextOffsets pDeltaTextOffsets);

}
