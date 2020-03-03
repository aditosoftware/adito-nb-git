package de.adito.git.api.data.diff;

/**
 * Defines a change on a word-basis
 *
 * @author m.kaspera, 27.02.2020
 */
public interface ILinePartChangeDelta
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

  /**
   * applies the given offset to the stored offsets of this delta
   *
   * @param pTextOffset offset for the text indices
   * @return IChangeDelta that has its values updated with the given offsets
   */
  ILinePartChangeDelta applyOffset(int pTextOffset);

  /**
   * Checks wether this delta is in conflict with another delta (affects same indizes)
   *
   * @param pLinePartChangeDelta the other linePartChangeDelta
   * @return true if the changes overlap, false otherwise
   */
  boolean isConflictingWith(ILinePartChangeDelta pLinePartChangeDelta);

}
