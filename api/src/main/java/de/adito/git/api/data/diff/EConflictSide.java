package de.adito.git.api.data.diff;

/**
 * Represents which side of the conflict should be chosen for some of the operations in the IMergeData
 * YOURS: The "local" side of the conflict
 * THEIRS: The "remote" side of the conflict
 *
 * @author m.kaspera, 06.03.2020
 */
public enum EConflictSide
{

  /**
   * The "local" side of the conflict
   */
  YOURS,
  /**
   * The "remote" side of the conflict
   */
  THEIRS;

  /**
   * inverts the given ConflictSide
   *
   * @param pConflictSide EConflictSide
   * @return opposite side of the given one
   */
  public static EConflictSide getOpposite(EConflictSide pConflictSide)
  {
    if (pConflictSide == YOURS)
      return THEIRS;
    return YOURS;
  }
}
