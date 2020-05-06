package de.adito.git.api.data.diff;

/**
 * Enum that signals which branch/commit/... to select
 *
 * @author m.kaspera 24.09.2018
 */
public enum EChangeSide
{

  NEW,

  OLD;

  /**
   * inverts the given EChangeSide
   *
   * @param pChangeSide EChangeSide
   * @return opposite side of the given one
   */
  public static EChangeSide invert(EChangeSide pChangeSide)
  {
    if (pChangeSide == NEW)
      return OLD;
    return NEW;
  }

}
