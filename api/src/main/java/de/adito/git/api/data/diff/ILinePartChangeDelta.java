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
   * @param pChangeSide Determines the side that the offset is added to
   * @return IChangeDelta that has its values updated with the given offsets
   */
  ILinePartChangeDelta applyOffset(int pTextOffset, EChangeSide pChangeSide);

  /**
   * Checks wether this delta is in conflict with another delta (affects same indizes)
   *
   * @param pLinePartChangeDelta the other linePartChangeDelta
   * @return true if the changes overlap, false otherwise
   */
  boolean isConflictingWith(ILinePartChangeDelta pLinePartChangeDelta);

  /**
   * Creates a new ILinePartChangeDelta based on the current one and the TextEvent. The start and endIndices of the new ILinePartChangeDelta are offset
   * based on the effects of the TextEvent
   *
   * @param pOffset     This is the startIndex of the text event
   * @param pLength     length of the text event, with pIsInsert determining if the given number of characters was added or subtracted
   * @param pIsInsert   true if an the text event was an insert, false if it was a delete event. Changes the meaning of the pLength parameter
   * @param pChangeSide The EChangeSide on which the text event happened
   * @return ILinePartChangeDelta that has the effects of the TextEvent incorporated
   */
  ILinePartChangeDelta processTextEvent(int pOffset, int pLength, boolean pIsInsert, EChangeSide pChangeSide);
}
