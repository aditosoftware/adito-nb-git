package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import javafx.util.Pair;
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
public class MergeDiffImpl implements IMergeDiff {

    private final IFileDiff baseSideDiff;
    private final IFileDiff mergeSideDiff;

    public MergeDiffImpl(IFileDiff pBaseSideDiff, IFileDiff pMergeSideDiff) {
        baseSideDiff = pBaseSideDiff;
        mergeSideDiff = pMergeSideDiff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFileDiff getDiff(CONFLICT_SIDE conflictSide) {
        if (conflictSide == CONFLICT_SIDE.YOURS) {
            return baseSideDiff;
        } else return mergeSideDiff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptChunk(IFileChangeChunk acceptedChunk, CONFLICT_SIDE conflictSide) {
        // list of changes the chunk should be applied to is the list of fileChangeChunks of the other side
        IFileChanges changes = getDiff(conflictSide == CONFLICT_SIDE.YOURS ? CONFLICT_SIDE.THEIRS : CONFLICT_SIDE.YOURS).getFileChanges();
        _insertChangeChunk(acceptedChunk, changes.getChangeChunks().blockingFirst());
        ((FileChangesImpl) changes).getSubject().onNext(changes.getChangeChunks().blockingFirst());
        _applyChangesSelf(acceptedChunk, () -> (FileChangesImpl) getDiff(conflictSide).getFileChanges());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void discardChange(IFileChangeChunk changeChunk, IMergeDiff.CONFLICT_SIDE conflict_side) {
        IFileChangeChunk replaceWith = new FileChangeChunkImpl(
                new Edit(changeChunk.getAStart(), changeChunk.getAEnd(), changeChunk.getBStart(), changeChunk.getBEnd()),
                changeChunk.getALines(),
                changeChunk.getBLines(),
                EChangeType.SAME);
        getDiff(conflict_side).getFileChanges().replace(changeChunk, replaceWith);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertText(@NotNull String text, int length, int offset, boolean insert) {
        if (insert) {
            _insertText(text, offset, CONFLICT_SIDE.YOURS);
            _insertText(text, offset, CONFLICT_SIDE.THEIRS);
        } else {
            _deleteText(length, offset, CONFLICT_SIDE.THEIRS);
            _deleteText(length, offset, CONFLICT_SIDE.YOURS);
        }
    }

    /**
     * @param text         the text that was inserted by the user as String
     * @param offset       the offset in comparison to the start of the document for the start of the user-input
     * @param conflictSide Side of the conflict
     */
    private void _insertText(String text, int offset, CONFLICT_SIDE conflictSide) {
        List<Pair<Integer, Integer>> affectedIndices = _getChunksForOffset(offset, text.length(), conflictSide);
        assert affectedIndices.size() == 1;
        IFileChangeChunk affectedChunk = getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst().get(affectedIndices.get(0).getKey());
        // get the part before and after the insert and add the added text in the middle
        String beforeOffsetPart = affectedChunk.getALines().substring(0, offset - affectedIndices.get(0).getValue());
        String afterOffsetPart = affectedChunk.getALines().substring(offset - affectedIndices.get(0).getValue());
        _updateChunkAndPropagateChanges(true, text, affectedChunk, affectedIndices.get(0).getKey(), beforeOffsetPart + text + afterOffsetPart, conflictSide);
    }

    /**
     * @param length       the number of characters that was deleted by the user
     * @param offset       the offset in comparison to the start of the document for the start of the user-input
     * @param conflictSide Side of the conflict
     */
    private void _deleteText(int length, int offset, CONFLICT_SIDE conflictSide) {
        List<Pair<Integer, Integer>> affectedChunksInfo = _getChunksForOffset(offset, length, conflictSide);
        List<IFileChangeChunk> changeChunkList = getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst();
        for (Pair<Integer, Integer> affectedChunkInfo : affectedChunksInfo) {
            int beforeOffsetPartEnd = offset - affectedChunkInfo.getValue() > 0 ? offset - affectedChunkInfo.getValue() : 0;
            // if this IFileChangeChunk has less lines after the offset/beginning of the chunk, set the afterOffsetPartStart to the end of the IFileChangeChunk
            int afterOffsetPartStart = changeChunkList.get(affectedChunkInfo.getKey()).getALines().length() < (offset + length) - affectedChunkInfo.getValue()
                    ? changeChunkList.get(affectedChunkInfo.getKey()).getALines().length()
                    : (offset + length) - affectedChunkInfo.getValue();
            // piece together the parts
            String beforeRemovedPart = changeChunkList.get(affectedChunkInfo.getKey()).getALines().substring(0, beforeOffsetPartEnd);
            String afterRemovedPart = changeChunkList.get(affectedChunkInfo.getKey()).getALines().substring(afterOffsetPartStart);
            String replacedPart = changeChunkList.get(affectedChunkInfo.getKey()).getALines().substring(beforeOffsetPartEnd, afterOffsetPartStart);
            _updateChunkAndPropagateChanges(false, replacedPart, changeChunkList.get(affectedChunkInfo.getKey()), affectedChunkInfo.getKey(), beforeRemovedPart + afterRemovedPart, conflictSide);
        }
    }

    /**
     * @param insert             if the user deleted or added text
     * @param text               the text that was either deleted or added by the user
     * @param affectedChunk      the IFileChangeChunk that was affected by the change
     * @param affectedChunkIndex the index of the affected chunk in the list of IFileChangeChunks
     * @param newALines          the new content in the A-lines as String
     * @param conflictSide       side of the conflict
     */
    private void _updateChunkAndPropagateChanges(boolean insert, String text, IFileChangeChunk affectedChunk, int affectedChunkIndex, String newALines, CONFLICT_SIDE conflictSide) {
        int additionalLines = 0;
        // if newlines were added/removed, find out how many
        if (text.contains("\n")) {
            additionalLines = (int) text.chars().filter(chr -> chr == '\n').count();
            // if the newlines were deleted the number of newlines is negative
            if (!insert)
                additionalLines = -additionalLines;
            _propagateAdditionalLines(getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst(), affectedChunkIndex + 1, additionalLines);
        }
        getDiff(conflictSide).getFileChanges().replace(affectedChunk, new FileChangeChunkImpl(
                new Edit(affectedChunk.getAStart(),
                        affectedChunk.getAEnd() + additionalLines,
                        affectedChunk.getBStart(),
                        affectedChunk.getBEnd()),
                newALines, affectedChunk.getBLines(),
                affectedChunk.getChangeType()));
    }

    /**
     * @param offset       the offset in comparison to the start of the document for the start of the user-input
     * @param lenString    the number of characters that was deleted/added by the user
     * @param conflictSide side of the conflict
     * @return List of Integer pairs that contain the
     * a) Index of the IFileChangeChunk affected by the change
     * b) The index of the first character in the IChangeChunk, measured in characters that come before it in the document
     */
    private List<Pair<Integer, Integer>> _getChunksForOffset(int offset, int lenString, CONFLICT_SIDE conflictSide) {
        List<Pair<Integer, Integer>> affectedIndices = new ArrayList<>();
        int currentOffset = 0;
        int nextOffset;
        List<IFileChangeChunk> changeChunks = getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst();
        if (changeChunks.size() > 0) {
            for (int index = 0; index < changeChunks.size(); index++) {
                // calculate the upper end of this IFileChangeChunk
                nextOffset = currentOffset + changeChunks.get(index).getALines().length();
                /*
                    add line if
                        a) offset is bigger than lower bound and smaller than upper bound (change is contained in this chunk)
                        b) lower bound is bigger than offset but smaller than offset + lenString (change starts in chunk before and continues in this one)
                  */
                if ((offset >= currentOffset && offset < nextOffset) || (offset + lenString >= currentOffset && offset < currentOffset)) {
                    affectedIndices.add(new Pair<>(index, currentOffset));
                }
                // the upper end of this IFileChangeChunk is the lower end of the next
                currentOffset = nextOffset;
            }
        }
        return affectedIndices;
    }

    /**
     * @param toInsert            the IFileChangeChunk whose changes should be inserted into the fileChangeChunkList
     * @param fileChangeChunkList list of IFileChangeChunks that should get the changes from toInsert applied to it
     */
    private void _insertChangeChunk(@NotNull IFileChangeChunk toInsert, @NotNull List<IFileChangeChunk> fileChangeChunkList) {
        List<Integer> affectedIndizes = MergeDiffImpl._affectedChunkIndices(toInsert, fileChangeChunkList);
        for (Integer num : affectedIndizes) {
            fileChangeChunkList.set(num, MergeDiffImpl._applyChange(toInsert, fileChangeChunkList.get(num)));
        }
        MergeDiffImpl._propagateAdditionalLines(fileChangeChunkList, affectedIndizes.get(affectedIndizes.size() - 1) + 1, (toInsert.getBEnd() - toInsert.getBStart()) - (toInsert.getAEnd() - toInsert.getAStart()));
    }

    /**
     * Accepts the changes from the B-side of the IFileChangeChunk, writes them to the A-side, saves
     * the changes into the list and adjusts the line numbers in the following IFileChangeChunks if needed
     *
     * @param toChangeChunk      IFileChangeChunk whose B-side is accepted -> copy B-side over to the A-side
     * @param fileChangeSupplier Supplier for the FileChangesImpl containing the IFileChangeChunk, to write the changed IFileChangeChunk back to the list
     */
    private void _applyChangesSelf(@NotNull IFileChangeChunk toChangeChunk, Supplier<FileChangesImpl> fileChangeSupplier) {
        synchronized (fileChangeSupplier.get()) {
            List<IFileChangeChunk> changeChunkList = fileChangeSupplier.get().getChangeChunks().blockingFirst();
            int indexInList = changeChunkList.indexOf(toChangeChunk);
            // create new IFileChangeChunks since IFileChangeChunks are effectively final
            Edit edit = new Edit(toChangeChunk.getAStart(), toChangeChunk.getAStart() + (toChangeChunk.getBEnd() - toChangeChunk.getBStart()), toChangeChunk.getBStart(), toChangeChunk.getBEnd());
            IFileChangeChunk changedChunk = new FileChangeChunkImpl(edit, toChangeChunk.getBLines(), toChangeChunk.getBLines(), EChangeType.SAME);
            // adjust line numbers in the following lines/changeChunks
            _propagateAdditionalLines(changeChunkList, indexInList + 1, (toChangeChunk.getBEnd() - toChangeChunk.getBStart()) - (toChangeChunk.getAEnd() - toChangeChunk.getAStart()));
            // save the changes to the list and fire a change on the list
            changeChunkList.set(indexInList, changedChunk);
            fileChangeSupplier.get().getSubject().onNext(changeChunkList);
        }
    }

    /**
     * @param toInsert IFileChangeChunk with the change that should be applied to the other IFileChangeChunk
     * @param toChange IFileChangeChunk that should have it's a-side lines changed in such a way that in contains the changes from toInsert
     * @return the IFileChangeChunk toChange with the changes from toInsert applied to it
     */
    @NotNull
    static IFileChangeChunk _applyChange(@NotNull IFileChangeChunk toInsert, @NotNull IFileChangeChunk toChange) {
        String[] toInsertLines = toInsert.getBLines().split("\n");
        String[] toChangeLines = toChange.getALines().split("\n");
        StringBuilder toChangeResult = new StringBuilder();
        // start accumulate lines before the affected part ------------------------------------------------------------------------------------------------------------
        /*
            count the number of lines that this chunk now has, so the edit can be changed accordingly
            start at -1, since in the end, one line means that aStart and aEnd are the same (with one line added, numLines goes to 0)
         */
        int numLines = 0;
        // Insert the lines that come before the change (if toChange chunk starts before toInsert chunk)
        if (toInsert.getChangeType() == EChangeType.ADD) {
            // collect all lines before the changed part, if the change is an insert the last line has to be added as well
            for (int index = 0; index < toInsert.getAStart() - toChange.getAStart(); index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        } else {
            // collect all lines before the changed part
            for (int index = 0; index < toInsert.getAStart() - toChange.getAStart(); index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        }
        // end accumulate lines before the affected part --------------------------------------------------------------------------------------------------------------
        // start accumulate changed lines -----------------------------------------------------------------------------------------------------------------------------
        // the last index of the changed part, depends on which chunk ends sooner
        int terminateAt;
        if (toInsert.getChangeType() == EChangeType.MODIFY) {
            // if the replace change started in a previous chunk, start from an offset (offset = number of lines of the change taken during the last chunk(s))
            int indexOffset = toChange.getAStart() - toInsert.getAStart() > 0 ? toChange.getAStart() - toInsert.getAStart() : 0;
            /*
                if the chunk in which to insert the change is longer, terminateAt is the number of lines in the change minus the lines processed in previous chunks
                else it is the length of this chunk
              */
            terminateAt = toChange.getAEnd() >= toInsert.getAEnd() ? toInsertLines.length - indexOffset : toChange.getAEnd() - toInsert.getAStart() - 1;
            // if the change starts on the last line of this chunk, we have to add that line. Without this, the line is not added
            if (toChange.getAEnd() == toInsert.getAStart() + 1) {
                terminateAt = 1;
            }
            // collect the changed lines
            for (int index = 0; index < terminateAt; index++) {
                toChangeResult.append(toInsertLines[index + indexOffset]).append("\n");
                numLines++;
            }
        } else {
            /*
                if the EChangeType is DELETE don't do anything (terminateAt = 0), else insert the number of lines in the change (EChangeType.ADD)
                no need for an offset here, since there either isn't anything to copy, or the copying is just inserting everything between two specific lines
             */
            terminateAt = toInsert.getBLines().equals("") ? 0 : toInsertLines.length;
            for (int index = 0; index < terminateAt; index++) {
                toChangeResult.append(toInsertLines[index]).append("\n");
                numLines++;
            }
        }
        // end accumulate changed lines -------------------------------------------------------------------------------------------------------------------------------
        // start accumulate lines after changed part ------------------------------------------------------------------------------------------------------------------
        // if the chunk at which to insert the changes ends later than the changed part
        if (toChange.getAEnd() > toInsert.getAEnd()) {
            // If the toChange chunk contains lines after the change, append these
            int startIndex = toInsert.getAEnd() - toChange.getAStart();
            for (int index = startIndex; index < toChangeLines.length; index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        }
        // end accumulate lines after changed part --------------------------------------------------------------------------------------------------------------------
        return new FileChangeChunkImpl(new Edit(toChange.getAStart(), toChange.getAStart() + numLines, toChange.getBStart(), toChange.getBEnd()),
                toChangeResult.toString(),
                toChange.getBLines(),
                toChange.getChangeType());
    }

    /**
     * checks which IFileChangeChunks in the list are affected by the IFileChangeChunk that gets inserted
     *
     * @param toInsert            the IFileChangeChunk to apply to the list of IFileChangeChunks
     * @param fileChangeChunkList List of IFileChangeChunks on which to apply toInsert
     * @return List of Integers with the indices of the affected IFileChangeChunks in the list
     */
    @NotNull
    static List<Integer> _affectedChunkIndices(@NotNull IFileChangeChunk toInsert, @NotNull List<IFileChangeChunk> fileChangeChunkList) {
        List<Integer> affectedChunks = new ArrayList<>();
        int intersectionIndex = 0;
        // Side A ist assumed to be the text from the fork-point
        while (fileChangeChunkList.get(intersectionIndex).getAEnd() <= toInsert.getAStart() && intersectionIndex < fileChangeChunkList.size()) {
            intersectionIndex++;
        }
        // all chunks before the affected area are now excluded
        while (fileChangeChunkList.get(intersectionIndex).getAStart() < toInsert.getAEnd() && intersectionIndex < fileChangeChunkList.size()) {
            affectedChunks.add(intersectionIndex);
            intersectionIndex++;
        }
        return affectedChunks;
    }

    /**
     * Adjust the start/end indices of the IFileChangeChunks in the list, starting from the given index
     *
     * @param fileChangeChunkList the list of IFileChangeChunks
     * @param listIndex           the first index that gets the offset
     * @param numLines            the number of lines the IFileChangeChunks have been set back
     */
    static void _propagateAdditionalLines(List<IFileChangeChunk> fileChangeChunkList, int listIndex, int numLines) {
        for (int index = listIndex; index < fileChangeChunkList.size(); index++) {
            // FileChangeChunks don't have setters, so create a new one
            IFileChangeChunk updated = new FileChangeChunkImpl(
                    new Edit(fileChangeChunkList.get(index).getAStart() + numLines, fileChangeChunkList.get(index).getAEnd() + numLines,
                            fileChangeChunkList.get(index).getBStart(), fileChangeChunkList.get(index).getBEnd()),
                    fileChangeChunkList.get(index).getALines(),
                    fileChangeChunkList.get(index).getBLines(),
                    fileChangeChunkList.get(index).getChangeType());
            // replace the current FileChangeChunk with the updated one
            fileChangeChunkList.set(index, updated);
        }
    }

}
