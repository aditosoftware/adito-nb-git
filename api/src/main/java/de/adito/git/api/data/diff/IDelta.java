package de.adito.git.api.data.diff;

import de.adito.git.impl.data.diff.EConflictType;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * Defines a minimum set of methods that a delta containing information about a change in a text has to have
 *
 * @author m.kaspera, 07.05.2020
 */
public interface IDelta
{
  /**
   * NOTE: This should not return the possible EChangeType.CONFLICTING, instead if the Delta is conflicting, the EConflictType should be set accordingly
   * -> therefore getConflictType should be checked when determining if this delta is conflicting
   *
   * @return Type of change that happened to the line part
   */
  @NotNull
  EChangeType getChangeType();

  /**
   * @return EConflictType that this delta has
   */
  @NotNull
  EConflictType getConflictType();

  /**
   * Get the index of the first character of this delta, the index should be the same as in a document containing all changeDeltas of a fileChange
   *
   * @param pChangeSide which side of the change should be taken
   * @return index of the first character of this delta as seen for the whole file
   */
  int getStartTextIndex(EChangeSide pChangeSide);

  /**
   * Get the index of the last character of this delta, the index should be the same as in a document containing all changeDeltas of a fileChange
   *
   * @param pChangeSide which side of the change should be taken
   * @return index of the last character  of this delta as seen for the whole file
   */
  int getEndTextIndex(EChangeSide pChangeSide);

  /**
   * Determines the primary diff color to be used for marking this delta in a dialog
   *
   * @return Color to use for marking this delta
   */
  @NotNull
  Color getDiffColor();

  /**
   * Determines the secondary diff color to be used for marking this delta in a dialog
   *
   * @return Color to use for marking this delta if the marking should be in the background
   */
  @NotNull
  Color getSecondaryDiffColor();
}
