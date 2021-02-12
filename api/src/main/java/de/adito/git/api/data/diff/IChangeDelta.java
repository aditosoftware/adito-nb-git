package de.adito.git.api.data.diff;

import de.adito.git.impl.data.diff.EConflictType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Defines a single change of a file, offers methods to accept or discard those changes and keeps track of the location of the change in relation to the whole text
 *
 * @author m.kaspera, 20.02.2020
 */
public interface IChangeDelta extends IDelta
{

  /**
   * gives information about whether the change was already accepted/discarded or is still pending
   *
   * @return EChangeStatus of the change
   */
  EChangeStatus getChangeStatus();

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
   * @param pChangeSide which side the offset should be applied to
   * @return IChangeDelta that has its values updated with the given offsets
   */
  IChangeDelta applyOffset(int pLineOffset, int pTextOffset, EChangeSide pChangeSide);

  /**
   * processes a text event that impacts this delta
   * There are 3 possible insert operation that can happen and 7 delete operations. In the following the | characters mark the position of the delta, the - characters
   * mark the position of the text event
   *
   * @formatter:off
   * INSERT 1
   *
   *     -
   * |
   * |
   *
   * This should be treated with an applyOffset instead of this method
   *
   * INSERT 2
   *
   * |
   * |
   *     -
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
   *     -
   * |   -
   * |   -
   * |
   *
   * DELETE 2
   *
   *     -
   * |   -
   * |   -
   *     -
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
   *     -
   *
   * DELETE 5
   *
   *     -
   *     -
   * |
   * |
   *
   * This should be done with a call to applyOffset and not this method
   *
   * DELETE 6
   *
   * |
   * |
   *     -
   *     -
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
   * @formatter:on
   *
   * @param pOffset            start index of the text event
   * @param pLength            number of characters that were deleted/inserted
   * @param pNumNewlinesBefore number of newLines that were added/removed before this delta
   * @param pNumNewlines       number of newLines that were added/removed inside this delta
   * @param pIsInsert          if the text event was an insert operation, false if it was a delete operation
   * @param pChangeSide        which side the text was inserted in
   * @return new IChangeDelta with the changes from the text event applied
   */
  IChangeDelta processTextEvent(int pOffset, int pLength, int pNumNewlinesBefore, int pNumNewlines, boolean pIsInsert, EChangeSide pChangeSide);

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
   * @param pConflictSide     Side of the conflict pOtherChangeDelta ist from
   * @return EConflictType denoting the type of conflict the deltas have. Can also be NONE, indicating there is no conflict
   */
  EConflictType isConflictingWith(IChangeDelta pOtherChangeDelta, @NotNull EConflictSide pConflictSide);

  /**
   * get the text of one side of this delta
   *
   * @param pChangeSide which side of the delta
   * @return text of the given side of this delta
   */
  String getText(EChangeSide pChangeSide);

  /**
   * checks if the given IChangeDelta represents the same change as this one
   *
   * @param pOtherChangeDelta the IChangeDelta to compare this delta to
   * @return true if the change is the same, false otherwise
   */
  boolean isSameChange(IChangeDelta pOtherChangeDelta);

  /**
   * Accepts the changes in this delta and returns a new IChangeDelta with the changed attributes
   *
   * @param pChangedSide   Side that is changed by accepting the change
   * @param pOffsetsChange IOffsetsChange giving the text- and lineOffset for the the endIndizes of the delta
   * @return new IChangeDelta
   */
  IChangeDelta acceptChange(EChangeSide pChangedSide, IOffsetsChange pOffsetsChange);

  /**
   * Appends the changes of the NEW side to the OLD side and returns a new IChangeDelta with the changed attributes
   *
   * @return new IChangeDelta
   */
  IChangeDelta appendChange();

  /**
   * Discards the Changes of this delta and returns a new IChangeDelta with the changed attributes
   *
   * @return new IChangeDelta
   */
  IChangeDelta discardChange();

  /**
   * Create a new ChangeDelta from the current one with the given status, should only be used to e.g. set the conflicting state if the delta clashes with another
   * one in a merge
   *
   * @param pChangeStatus status to set
   * @return new ChangeDelta
   */
  IChangeDelta setChangeStatus(IChangeStatus pChangeStatus);
}
