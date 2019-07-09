package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

  /**
   * checks which IFileChangeChunks in the list are affected by the IFileChangeChunk that gets inserted
   *
   * @param pToInsert            the IFileChangeChunk to apply to the list of IFileChangeChunks
   * @param pFileChangeChunkList List of IFileChangeChunks on which to apply toInsert
   * @return List of Integers with the indices of the affected IFileChangeChunks in the list
   */
  @NotNull
  static List<Integer> affectedChunkIndices(@NotNull IFileChangeChunk pToInsert, @NotNull List<IFileChangeChunk> pFileChangeChunkList)
  {
    List<Integer> affectedChunks = new ArrayList<>();
    int intersectionIndex = 0;
    // Side A ist assumed to be the text from the fork-point
    while (intersectionIndex < pFileChangeChunkList.size() - 1
        && pFileChangeChunkList.get(intersectionIndex).getEnd(EChangeSide.OLD) <= pToInsert.getStart(EChangeSide.OLD))
    {
      intersectionIndex++;
      if (pFileChangeChunkList.get(intersectionIndex).getEnd(EChangeSide.OLD) == pToInsert.getStart(EChangeSide.OLD)
          && pToInsert.getStart(EChangeSide.OLD) == pToInsert.getEnd(EChangeSide.OLD))
      {
        break;
      }
    }
    if (pToInsert.getChangeType() == EChangeType.ADD
        && pToInsert.getStart(EChangeSide.OLD) == pFileChangeChunkList.get(intersectionIndex).getStart(EChangeSide.OLD))
    {
      affectedChunks.add(intersectionIndex);
    }
    // all chunks before the affected area are now excluded
    while (intersectionIndex < pFileChangeChunkList.size()
        && pFileChangeChunkList.get(intersectionIndex).getStart(EChangeSide.OLD) < pToInsert.getEnd(EChangeSide.OLD))
    {
      if (!affectedChunks.contains(intersectionIndex))
        affectedChunks.add(intersectionIndex);
      intersectionIndex++;
    }
    return affectedChunks;
  }

}
