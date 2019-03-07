package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents the state of the files in a three-way merger:
 * <p>
 * the A-side in each of the IFileDiffs represents the fork-point state at first,
 * and changes with the changeChunks/changes the user accepts from either side.
 * The A-sides should always be the same in both of the IFileDiffs
 * <p>
 * The B-side represents the state of the current branch or the state of the branch
 * that is getting merged with parent, depending on which IFileDiff is looked at.
 * The B-side is staying the same throughout the merge-process
 *
 * @author m.kaspera 18.10.2018
 */
public class MergeDiffImpl implements IMergeDiff
{

  private final IFileDiff baseSideDiff;
  private final IFileDiff mergeSideDiff;

  public MergeDiffImpl(IFileDiff pBaseSideDiff, IFileDiff pMergeSideDiff)
  {
    baseSideDiff = pBaseSideDiff;
    mergeSideDiff = pMergeSideDiff;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IFileDiff getDiff(CONFLICT_SIDE pConflictSide)
  {
    if (pConflictSide == CONFLICT_SIDE.YOURS)
    {
      return baseSideDiff;
    }
    else return mergeSideDiff;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void acceptChunk(IFileChangeChunk pAcceptedChunk, CONFLICT_SIDE pConflictSide)
  {
    // list of changes the chunk should be applied to is the list of fileChangeChunks of the other side
    IFileChanges changes = getDiff(pConflictSide == CONFLICT_SIDE.YOURS ? CONFLICT_SIDE.THEIRS : CONFLICT_SIDE.YOURS).getFileChanges();
    IEditorChange editorChange = _insertChangeChunk(pAcceptedChunk, changes.getChangeChunks().blockingFirst().getNewValue());
    EditorChangeEventImpl editorChangeEvent = new EditorChangeEventImpl(editorChange, new EditorChangeImpl(0, -1, null));
    ((FileChangesImpl) changes).getSubject().onNext(new FileChangesEventImpl(true, changes.getChangeChunks().blockingFirst().getNewValue(),
                                                                             editorChangeEvent));
    _applyChangesSelf(pAcceptedChunk, () -> (FileChangesImpl) getDiff(pConflictSide).getFileChanges());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void discardChange(IFileChangeChunk pChangeChunk, IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    IFileChangeChunk replaceWith = new FileChangeChunkImpl(
        new Edit(pChangeChunk.getStart(EChangeSide.OLD), pChangeChunk.getEnd(EChangeSide.OLD),
                 pChangeChunk.getStart(EChangeSide.NEW), pChangeChunk.getEnd(EChangeSide.NEW)),
        pChangeChunk.getLines(EChangeSide.OLD),
        pChangeChunk.getLines(EChangeSide.NEW),
        EChangeType.SAME);
    getDiff(pConflictSide).getFileChanges().replace(pChangeChunk, replaceWith, true);
    getDiff(pConflictSide == CONFLICT_SIDE.YOURS ? CONFLICT_SIDE.THEIRS : CONFLICT_SIDE.YOURS).getFileChanges().emptyUpdate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insertText(@NotNull String pText, int pLength, int pOffset, boolean pInsert)
  {
    if (pInsert)
    {
      _insertText(pText, pOffset, CONFLICT_SIDE.YOURS);
      _insertText(pText, pOffset, CONFLICT_SIDE.THEIRS);
    }
    else
    {
      _deleteText(pLength, pOffset, CONFLICT_SIDE.THEIRS);
      _deleteText(pLength, pOffset, CONFLICT_SIDE.YOURS);
    }
  }

  /**
   * @param pText         the text that was inserted by the user as String
   * @param pOffset       the offset in comparison to the start of the document for the start of the user-input
   * @param pConflictSide Side of the conflict
   */
  private void _insertText(String pText, int pOffset, CONFLICT_SIDE pConflictSide)
  {
    List<Pair<Integer, Integer>> affectedIndices = _getChunksForOffset(pOffset, 0, pConflictSide);
    assert affectedIndices.size() == 1;
    IFileChangeChunk affectedChunk = getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst()
        .getNewValue().get(affectedIndices.get(0).getKey());
    // get the part before and after the insert and add the added text in the middle
    String beforeOffsetPart = affectedChunk.getLines(EChangeSide.OLD).substring(0, pOffset - affectedIndices.get(0).getValue());
    String afterOffsetPart = affectedChunk.getLines(EChangeSide.OLD).substring(pOffset - affectedIndices.get(0).getValue());
    _updateChunkAndPropagateChanges(true, pText, affectedChunk, affectedIndices.get(0).getKey(),
                                    beforeOffsetPart + pText + afterOffsetPart, pConflictSide);
  }

  /**
   * @param pLength       the number of characters that was deleted by the user
   * @param pOffset       the offset in comparison to the start of the document for the start of the user-input
   * @param pConflictSide Side of the conflict
   */
  private void _deleteText(int pLength, int pOffset, CONFLICT_SIDE pConflictSide)
  {
    List<Pair<Integer, Integer>> affectedChunksInfo = _getChunksForOffset(pOffset, pLength, pConflictSide);
    List<IFileChangeChunk> changeChunkList = getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue();
    for (Pair<Integer, Integer> affectedChunkInfo : affectedChunksInfo)
    {
      int beforeOffsetPartEnd = pOffset - affectedChunkInfo.getValue() > 0 ? pOffset - affectedChunkInfo.getValue() : 0;
      // if this IFileChangeChunk has less lines after the offset of the chunk, set the afterOffsetPartStart to the end of the IFileChangeChunk
      int afterOffsetPartStart = changeChunkList.get(affectedChunkInfo.getKey()).getLines(EChangeSide.OLD).length()
          < (pOffset + pLength) - affectedChunkInfo.getValue()
          ? changeChunkList.get(affectedChunkInfo.getKey()).getLines(EChangeSide.OLD).length()
          : (pOffset + pLength) - affectedChunkInfo.getValue();
      // piece together the parts
      String beforeRemovedPart = changeChunkList.get(affectedChunkInfo.getKey()).getLines(EChangeSide.OLD).substring(0, beforeOffsetPartEnd);
      String afterRemovedPart = changeChunkList.get(affectedChunkInfo.getKey()).getLines(EChangeSide.OLD).substring(afterOffsetPartStart);
      String replacedPart = changeChunkList.get(affectedChunkInfo.getKey()).getLines(EChangeSide.OLD)
          .substring(beforeOffsetPartEnd, afterOffsetPartStart);
      _updateChunkAndPropagateChanges(false, replacedPart, changeChunkList.get(affectedChunkInfo.getKey()),
                                      affectedChunkInfo.getKey(), beforeRemovedPart + afterRemovedPart, pConflictSide);
    }
  }

  /**
   * @param pInsert             if the user deleted or added text
   * @param pText               the text that was either deleted or added by the user
   * @param pAffectedChunk      the IFileChangeChunk that was affected by the change
   * @param pAffectedChunkIndex the index of the affected chunk in the list of IFileChangeChunks
   * @param pNewALines          the new content in the A-lines as String
   * @param pConflictSide       side of the conflict
   */
  private void _updateChunkAndPropagateChanges(boolean pInsert, String pText, IFileChangeChunk pAffectedChunk,
                                               int pAffectedChunkIndex, String pNewALines, CONFLICT_SIDE pConflictSide)
  {
    // UI should only be updated here when the inserted/deleted text contains a newline.
    // If it doesn't, text is already shown by the pane so no need to update it again with the same information
    boolean updateUI = false;
    int additionalLines = 0;
    // if newlines were added/removed, find out how many
    if (pText.contains("\n"))
    {
      additionalLines = (int) pText.chars().filter(chr -> chr == '\n').count();
      // if the newlines were deleted the number of newlines is negative
      if (!pInsert)
        additionalLines = -additionalLines;
      propagateAdditionalLines(getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue(),
                               pAffectedChunkIndex + 1, additionalLines);
    }
    // if the user deleted text and all the lines of the chunk were deleted, the chunk is irrecoverable and should be
    // disregarded hence forth -> EChangeType.SAME
    EChangeType changeType = !pInsert && pNewALines.length() == 0 ? EChangeType.SAME : pAffectedChunk.getChangeType();
    getDiff(pConflictSide).getFileChanges().replace(pAffectedChunk, new FileChangeChunkImpl(
        new Edit(pAffectedChunk.getStart(EChangeSide.OLD),
                 pAffectedChunk.getEnd(EChangeSide.OLD) + additionalLines,
                 pAffectedChunk.getStart(EChangeSide.NEW),
                 pAffectedChunk.getEnd(EChangeSide.NEW)),
        pNewALines, pAffectedChunk.getLines(EChangeSide.NEW),
        changeType), updateUI);
  }

  /**
   * @param pOffset       the offset in comparison to the start of the document for the start of the user-input
   * @param pLenString    the number of characters that was deleted/added by the user
   * @param pConflictSide side of the conflict
   * @return List of Integer pairs that contain the
   * a) Index of the IFileChangeChunk affected by the change
   * b) The index of the first character in the IChangeChunk, measured in characters that come before it in the document
   */
  private List<Pair<Integer, Integer>> _getChunksForOffset(int pOffset, int pLenString, CONFLICT_SIDE pConflictSide)
  {
    List<Pair<Integer, Integer>> affectedIndices = new ArrayList<>();
    int currentOffset = 0;
    int nextOffset;
    List<IFileChangeChunk> changeChunks = getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue();
    if (!changeChunks.isEmpty())
    {
      for (int index = 0; index < changeChunks.size(); index++)
      {
        // calculate the upper end of this IFileChangeChunk
        nextOffset = currentOffset + changeChunks.get(index).getLines(EChangeSide.OLD).length();
        /*
            add line if
                a) offset is bigger than lower bound and smaller than upper bound (change is contained in this chunk)
                b) lower bound is bigger than offset but smaller than offset + lenString (change starts in chunk before and continues in this one)
                c) offset is equal to both current and nextOffset, i.e. the chunk has 0 length and sits on the offset. Since there can be several such
                    chunks when only one of them matters and taking several leads to issues when inserting, take only one
          */
        if ((pOffset >= currentOffset && pOffset < nextOffset) || (pOffset + pLenString >= currentOffset && pOffset < currentOffset)
            || (pOffset == currentOffset && currentOffset == nextOffset && affectedIndices.isEmpty()))
        {
          if (!affectedIndices.isEmpty() && affectedIndices.get(0).getValue() == currentOffset)
          {
            affectedIndices.remove(0);
          }
          affectedIndices.add(Pair.of(index, currentOffset));
        }
        // the upper end of this IFileChangeChunk is the lower end of the next
        currentOffset = nextOffset;
      }
    }
    return affectedIndices;
  }

  /**
   * @param pToInsert            the IFileChangeChunk whose changes should be inserted into the fileChangeChunkList
   * @param pFileChangeChunkList list of IFileChangeChunks that should get the changes from toInsert applied to it
   */
  private static IEditorChange _insertChangeChunk(@NotNull IFileChangeChunk pToInsert, @NotNull List<IFileChangeChunk> pFileChangeChunkList)
  {
    List<Integer> affectedIndizes = MergeDiffImpl.affectedChunkIndices(pToInsert, pFileChangeChunkList);
    int startOffset = FileChangesImpl.getOffsetForChunk(affectedIndizes.get(0), pFileChangeChunkList, EChangeSide.OLD);
    int numCharsToDelete = 0;
    for (Integer num : affectedIndizes)
    {
      numCharsToDelete += pFileChangeChunkList.get(num).getEditorLines(EChangeSide.OLD).length();
      pFileChangeChunkList.set(num, MergeDiffImpl.applyChange(pToInsert, pFileChangeChunkList.get(num)));
    }
    MergeDiffImpl.propagateAdditionalLines(pFileChangeChunkList, affectedIndizes.get(affectedIndizes.size() - 1) + 1,
                                           (pToInsert.getEnd(EChangeSide.NEW) - pToInsert.getStart(EChangeSide.NEW))
                                               - (pToInsert.getEnd(EChangeSide.OLD) - pToInsert.getStart(EChangeSide.OLD)));
    return new EditorChangeImpl(startOffset, numCharsToDelete, pToInsert.getLines(EChangeSide.NEW));
  }

  /**
   * Accepts the changes from the B-side of the IFileChangeChunk, writes them to the A-side, saves
   * the changes into the list and adjusts the line numbers in the following IFileChangeChunks if needed
   *
   * @param pToChangeChunk      IFileChangeChunk whose B-side is accepted -> copy B-side over to the A-side
   * @param pFileChangeSupplier Supplier for the FileChangesImpl containing the IFileChangeChunk, writes the changed IFileChangeChunk back to the list
   */
  private static void _applyChangesSelf(@NotNull IFileChangeChunk pToChangeChunk, Supplier<FileChangesImpl> pFileChangeSupplier)
  {
    synchronized (pFileChangeSupplier.get())
    {
      List<IFileChangeChunk> changeChunkList = pFileChangeSupplier.get().getChangeChunks().blockingFirst().getNewValue();
      int indexInList = changeChunkList.indexOf(pToChangeChunk);
      EditorChangeImpl editorChange = new EditorChangeImpl(FileChangesImpl.getOffsetForChunk(indexInList, changeChunkList, EChangeSide.OLD),
                                                           pToChangeChunk.getEditorLines(EChangeSide.OLD).length(),
                                                           pToChangeChunk.getLines(EChangeSide.NEW));
      // create new IFileChangeChunks since IFileChangeChunks are effectively final
      Edit edit = new Edit(pToChangeChunk.getStart(EChangeSide.OLD), pToChangeChunk.getStart(EChangeSide.OLD)
          + (pToChangeChunk.getEnd(EChangeSide.NEW) - pToChangeChunk.getStart(EChangeSide.NEW)), pToChangeChunk.getStart(EChangeSide.NEW),
                           pToChangeChunk.getEnd(EChangeSide.NEW));
      IFileChangeChunk changedChunk = new FileChangeChunkImpl(edit, pToChangeChunk.getLines(EChangeSide.NEW),
                                                              pToChangeChunk.getLines(EChangeSide.NEW), EChangeType.SAME);
      // adjust line numbers in the following lines/changeChunks
      propagateAdditionalLines(changeChunkList, indexInList + 1, (pToChangeChunk.getEnd(EChangeSide.NEW)
          - pToChangeChunk.getStart(EChangeSide.NEW)) - (pToChangeChunk.getEnd(EChangeSide.OLD) - pToChangeChunk.getStart(EChangeSide.OLD)));
      // save the changes to the list and fire a change on the list
      changeChunkList.set(indexInList, changedChunk);
      pFileChangeSupplier.get().getSubject().onNext(new FileChangesEventImpl(true, changeChunkList,
                                                                             new EditorChangeEventImpl(editorChange,
                                                                                                       new EditorChangeImpl(0, -1, null))));
    }
  }

  /**
   * @param pToInsert IFileChangeChunk with the change that should be applied to the other IFileChangeChunk
   * @param pToChange IFileChangeChunk that should have it's a-side lines changed in such a way that in contains the changes from toInsert
   * @return the IFileChangeChunk toChange with the changes from toInsert applied to it
   */
  @NotNull
  static IFileChangeChunk applyChange(@NotNull IFileChangeChunk pToInsert, @NotNull IFileChangeChunk pToChange)
  {
    String[] toInsertLines = pToInsert.getLines(EChangeSide.NEW).split("\n");
    String[] toChangeLines = pToChange.getLines(EChangeSide.OLD).split("\n");
    StringBuilder toChangeResult = new StringBuilder();
    // start accumulate lines before the affected part -----------------------------------------------------------------------------------------------
        /*
            count the number of lines that this chunk now has, so the edit can be changed accordingly
            start at -1, since in the end, one line means that aStart and aEnd are the same (with one line added, numLines goes to 0)
         */
    int numLines = 0;
    // Insert the lines that come before the change (if toChange chunk starts before toInsert chunk)
    // collect all lines before the changed part
    for (int index = 0; index < pToInsert.getStart(EChangeSide.OLD) - pToChange.getStart(EChangeSide.OLD); index++)
    {
      toChangeResult.append(toChangeLines[index]).append("\n");
      numLines++;
    }
    // end accumulate lines before the affected part -------------------------------------------------------------------------------------------------
    // start accumulate changed lines ----------------------------------------------------------------------------------------------------------------
    // the last index of the changed part, depends on which chunk ends sooner
    int terminateAt;
    if (pToInsert.getChangeType() == EChangeType.MODIFY)
    {
      // if the replace change started in a previous chunk, start from an offset(offset = number of lines of the change taken during the last chunk/s)
      int indexOffset = pToChange.getStart(EChangeSide.OLD) - pToInsert.getStart(EChangeSide.OLD) > 0
          ? pToChange.getStart(EChangeSide.OLD) - pToInsert.getStart(EChangeSide.OLD) : 0;
      /*
          if the chunk in which to insert the change is longer, terminateAt is the number of lines in the change minus the lines processed in previous
          chunks else it is the length of this chunk
        */
      terminateAt = pToChange.getEnd(EChangeSide.OLD) >= pToInsert.getEnd(EChangeSide.OLD)
          ? toInsertLines.length - indexOffset : pToChange.getEnd(EChangeSide.OLD) - pToInsert.getStart(EChangeSide.OLD) - 1;
      // if the change starts on the last line of this chunk, we have to add that line. Without this, the line is not added
      if (pToChange.getEnd(EChangeSide.OLD) == pToInsert.getStart(EChangeSide.OLD) + 1 && terminateAt == 0)
      {
        terminateAt = 1;
      }
      // collect the changed lines
      for (int index = 0; index < terminateAt; index++)
      {
        toChangeResult.append(toInsertLines[index + indexOffset]).append("\n");
        numLines++;
      }
    }
    else
    {
    /*
        if the EChangeType is DELETE don't do anything (terminateAt = 0), else insert the number of lines in the change (EChangeType.ADD)
        no need for an offset here, since there either isn't anything to copy, or the copying is just inserting everything between two specific lines
     */
      terminateAt = "".equals(pToInsert.getLines(EChangeSide.NEW)) ? 0 : toInsertLines.length;
      for (int index = 0; index < terminateAt; index++)
      {
        toChangeResult.append(toInsertLines[index]).append("\n");
        numLines++;
      }
    }
    // end accumulate changed lines ------------------------------------------------------------------------------------------------------------------
    // start accumulate lines after changed part -----------------------------------------------------------------------------------------------------
    // if the chunk at which to insert the changes ends later than the changed part
    if (pToChange.getEnd(EChangeSide.OLD) > pToInsert.getEnd(EChangeSide.OLD))
    {
      // If the toChange chunk contains lines after the change, append these
      int startIndex = pToInsert.getEnd(EChangeSide.OLD) - pToChange.getStart(EChangeSide.OLD);
      for (int index = startIndex; index < toChangeLines.length; index++)
      {
        toChangeResult.append(toChangeLines[index]).append("\n");
        numLines++;
      }
    }
    // end accumulate lines after changed part -------------------------------------------------------------------------------------------------------
    return new FileChangeChunkImpl(new Edit(pToChange.getStart(EChangeSide.OLD),
                                            pToChange.getStart(EChangeSide.OLD) + numLines,
                                            pToChange.getStart(EChangeSide.NEW),
                                            pToChange.getEnd(EChangeSide.NEW)),
                                   toChangeResult.toString(),
                                   pToChange.getLines(EChangeSide.NEW),
                                   pToChange.getChangeType());
  }

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
    while (intersectionIndex < pFileChangeChunkList.size()
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

  /**
   * Adjust the start/end indices of the IFileChangeChunks in the list, starting from the given index
   *
   * @param pFileChangeChunkList the list of IFileChangeChunks
   * @param pListIndex           the first index that gets the offset
   * @param pNumLines            the number of lines the IFileChangeChunks have been set back
   */
  static void propagateAdditionalLines(List<IFileChangeChunk> pFileChangeChunkList, int pListIndex, int pNumLines)
  {
    for (int index = pListIndex; index < pFileChangeChunkList.size(); index++)
    {
      // FileChangeChunks don't have setters, so create a new one
      IFileChangeChunk updated = new FileChangeChunkImpl(
          new Edit(pFileChangeChunkList.get(index).getStart(EChangeSide.OLD) + pNumLines,
                   pFileChangeChunkList.get(index).getEnd(EChangeSide.OLD) + pNumLines,
                   pFileChangeChunkList.get(index).getStart(EChangeSide.NEW),
                   pFileChangeChunkList.get(index).getEnd(EChangeSide.NEW)),
          pFileChangeChunkList.get(index).getLines(EChangeSide.OLD),
          pFileChangeChunkList.get(index).getLines(EChangeSide.NEW),
          pFileChangeChunkList.get(index).getChangeType());
      // replace the current FileChangeChunk with the updated one
      pFileChangeChunkList.set(index, updated);
    }
  }

}
