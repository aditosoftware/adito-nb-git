package de.adito.git.api.data;

/**
 * Interface defining a data structure that contains information about a Merge and offers methods to change the state of the result of the merge
 *
 * @author m.kaspera 18.10.2018
 */
public interface IMergeDiff
{

  /**
   * get the IFileDiff of the specified conflict side
   *
   * @param conflictSide CONFLICT_SIDE describing if the diff from base-side or branch-to-merge-side to fork-point is wanted
   * @return IFileDiff for the comparison branch-to-merge to fork-point commit (CONFLICT_SIDE.THEIRS) or base-side to fork-point commit (CONFLICT_SIDE.YOURS)
   */
  IFileDiff getDiff(CONFLICT_SIDE conflictSide);

  /**
   * accepts all changes from the given chunk and applies these changes to the other conflict side
   *
   * @param acceptedChunk the change that should be accepted and added to the fork-point commit
   * @param conflictSide  CONFLICT_SIDE from which the chunk originates
   */
  void acceptChunk(IFileChangeChunk acceptedChunk, CONFLICT_SIDE conflictSide);

  /**
   * discards the specified changes by the given chunk
   *
   * @param acceptedChunk the change that should be discarded
   * @param conflictSide  CONFLICT_SIDE from which the chunk originates
   */
  void discardChange(IFileChangeChunk acceptedChunk, CONFLICT_SIDE conflictSide);

  /**
   * inserts a line of text to the fork-point commit text
   *
   * @param text   the text that was inserted, empty string if remove operation
   * @param length the number of characters that were inserted/removed
   * @param offset the offset of the place of insertion from the beginning of the document
   * @param insert true if the text is inserted, false if the text is removed/deleted
   */
  void insertText(String text, int length, int offset, boolean insert);

  /**
   * Represents which side of the conflict should be chosen for some of the operations in the IMergeDiff
   * YOURS: The "local" side of the conflict
   * THEIRS: The "remote" side of the conflict
   */
  enum CONFLICT_SIDE
  {YOURS, THEIRS}

}
