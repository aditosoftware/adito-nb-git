package de.adito.git.api.data.diff;

/**
 * Defines a minimum set of methods that a delta containing information about a change in a text has to have
 *
 * @author m.kaspera, 07.05.2020
 */
public interface IDelta
{
  /**
   * @return Type of change that happened to the line part
   */
  EChangeType getChangeType();

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
}
