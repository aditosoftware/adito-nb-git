package de.adito.git.api.data.diff;

import java.util.List;

/**
 * Defines a single change of a file, offers methods to accept or discard those changes and keeps track of the location of the change in relation to the whole text
 *
 * @author m.kaspera, 20.02.2020
 */
public interface IChangeDelta
{

  /**
   * @return the type of change that occurred
   */
  IChangeStatus getChangeStatus();

  /**
   * get the number of the first line that is affected by this change
   *
   * @param pChangeSide which side of the change should be taken
   * @return line that the change on the chosen side starts
   */
  int getStartLine(EChangeSide pChangeSide);

  /**
   * get the number of the last line that is affected by this change
   *
   * @param pChangeSide which side of the change should be taken
   * @return line that the change on the chosen side ends
   */
  int getEndLine(EChangeSide pChangeSide);

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
   * applies the given offsets to the stored offsets of this delta
   *
   * @param pLineOffset offset for the lines
   * @param pTextOffset offset for the text indices
   * @return IChangeDelta that has its values updated with the given offsets
   */
  IChangeDelta applyOffset(int pLineOffset, int pTextOffset);

  /**
   * processes a text event that impacts this delta
   * There are 3 possible insert operation that can happen and 7 delete operations. In the following the | characters mark the position of the delta, the - characters
   * mark the position of the text event
   *
   * INSERT 1
   *
   * -
   * |
   * |
   *
   * This should be treated with an applyOffset instead of this method
   *
   * INSERT 2
   *
   * |
   * |
   * -
   *
   * This does not affect the delta at all
   *
   * INSERT 3
   *
   * |
   * |   -
   * |
   *
   * DELETE 1
   *
   * -
   * |   -
   * |   -
   * |
   *
   * DELETE 2
   *
   * -
   * |   -
   * |   -
   * -
   *
   * DELETE 3
   *
   * |
   * |   -
   * |   -
   * |
   *
   * DELETE 4
   *
   * |
   * |   -
   * |   -
   * -
   *
   * DELETE 5
   *
   * -
   * -
   * |
   * |
   *
   * This should be done with a call to applyOffset and not this method
   *
   * DELETE 6
   *
   * |
   * |
   * -
   * -
   *
   * This does not affect the chunk at all
   *
   * DELETE 7
   *
   * |   -
   * |   -
   * |   -
   *
   * This is a special case of 3
   *
   * @param pOffset            start index of the text event
   * @param pLength            number of characters that were deleted/inserted
   * @param pNumNewlinesBefore number of newLines that were added/removed before this delta
   * @param pNumNewlines       number of newLines that were added/removed inside this delta
   * @param pIsInsert          if the text event was an insert operation, false if it was a delete operation
   * @return new IChangeDelta with the changes from the text event applied
   */
  IChangeDelta processTextEvent(int pOffset, int pLength, int pNumNewlinesBefore, int pNumNewlines, boolean pIsInsert);

  /**
   * Get a list of changes of this delta on a word-basis
   *
   * @return List describing the changes of this delta on a word-basis
   */
  List<ILinePartChangeDelta> getLinePartChanges();

  /**
   * Checks wether this delta is in conflict with another delta (affects same indizes)
   *
   * @param pOtherChangeDelta the other IChangeDelta
   * @return true if the changes overlap, false otherwise
   */
  boolean isConflictingWith(IChangeDelta pOtherChangeDelta);

  /**
   * get the text of one side of this delta
   *
   * @param pChangeSide which side of the delta
   * @return text of the given side of this delta
   */
  String getText(EChangeSide pChangeSide);

  /**
   * Accepts the changes in this delta and returns a new IChangeDelta with the changed attributes
   *
   * @return new IChangeDelta
   */
  IChangeDelta acceptChange();

  /**
   * Discards the Changes of this delta and returns a new IChangeDelta with the changed attributes
   *
   * @return new IChangeDelta
   */
  IChangeDelta discardChange();
}
