package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
     *
     * {@inheritDoc}
     */
    @Override
    public IFileDiff getBaseSideDiff() {
        return baseSideDiff;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public IFileDiff getMergeSideDiff() {
        return mergeSideDiff;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void insertBaseSideChunk(IFileChangeChunk toInsert) {
        IFileChanges changes = mergeSideDiff.getFileChanges();
        _insertChangeChunk(toInsert, changes.getChangeChunks().blockingFirst());
        ((FileChangesImpl) changes).getSubject().onNext(changes.getChangeChunks().blockingFirst());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void insertMergeSideChunk(IFileChangeChunk toInsert) {
        IFileChanges changes = baseSideDiff.getFileChanges();
        _insertChangeChunk(toInsert, baseSideDiff.getFileChanges().getChangeChunks().blockingFirst());
        ((FileChangesImpl) changes).getSubject().onNext(changes.getChangeChunks().blockingFirst());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void insertText(String text) {

    }

    /**
     *
     * @param toInsert the IFileChangeChunk whose changes should be inserted into the fileChangeChunkList
     * @param fileChangeChunkList list of IFileChangeChunks that should get the changes from toInsert applied to it
     */
    private void _insertChangeChunk(@NotNull IFileChangeChunk toInsert, @NotNull List<IFileChangeChunk> fileChangeChunkList) {
        List<Integer> affectedIndizes = MergeDiffImpl._affectedChunkIndizes(toInsert, fileChangeChunkList);
        for (Integer num : affectedIndizes) {
            fileChangeChunkList.set(num, MergeDiffImpl._applyChange(toInsert, fileChangeChunkList.get(num)));
        }
        MergeDiffImpl._propagateAdditionalLines(fileChangeChunkList, affectedIndizes.get(affectedIndizes.size() - 1), (toInsert.getBEnd() - toInsert.getBStart()) - (toInsert.getAEnd() - toInsert.getAStart()));
    }

    /**
     *
     * @param toInsert IFileChangeChunk with the change that should be applied to the other IFileChangeChunk
     * @param toChange IFileChangeChunk that should have it's a-side lines changed in such a way that in contains the changes from toInsert
     * @return the IFileChangeChunk toChange with the changes from toInsert applied to it
     */
    @NotNull
    static IFileChangeChunk _applyChange(@NotNull IFileChangeChunk toInsert, @NotNull IFileChangeChunk toChange) {
        String[] toInsertLines = toInsert.getBLines().split("\n");
        String[] toChangeLines = toChange.getALines().split("\n");
        StringBuilder toChangeResult = new StringBuilder();
        /*
            count the number of lines that this chunk now has, so the edit can be changed accordingly
            start at -1, since in the end, one line means that aStart and aEnd are the same (with one line added, numLines goes to 0)
         */
        int numLines = -1;
        // Insert the lines that come before the change (if toChange chunk starts before toInsert chunk)
        if (toInsert.getChangeType() == EChangeType.ADD) {
            // collect all lines before the changed part, if the change is an insert the last line has to be added as well
            for (int index = 0; index <= toInsert.getAStart() - toChange.getAStart(); index++) {
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
        // the last index of the changed part, depends on which chunk ends sooner
        int terminateAt;
        if (toInsert.getChangeType() == EChangeType.MODIFY) {
            // if the replace change started in a previous chunk, start from an offset (offset = number of lines of the change taken during the last chunk(s))
            int indexOffset = Math.abs(toInsert.getAStart() - toChange.getAStart());
            /*
                if the chunk in which to insert the change is longer, terminateAt is the number of lines in the change minus the lines processed in previous chunks
                else it is the length of this chunk
              */
            terminateAt = toChange.getAEnd() >= toInsert.getAEnd() ? toInsertLines.length - indexOffset : toChange.getAEnd() - toInsert.getAStart();
            // if the change starts on the last line of this chunk, we have to add that line. Without this, the line is not added
            if (toChange.getAEnd() == toInsert.getAStart()) {
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
        // if the chunk at which to insert the changes ends later than the changed part
        if (toChange.getAEnd() > toInsert.getAEnd()) {
            // If the toChange chunk contains lines after the change, append these
            for (int index = toInsert.getAEnd() - toChange.getAStart() + 1; index < toChangeLines.length; index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        }
        return new FileChangeChunkImpl(new Edit(toChange.getAStart(), toChange.getAStart() + numLines, toChange.getBStart(), toChange.getBEnd()), toChangeResult.toString(), toChange.getBLines());
    }

    /**
     * checks which IFileChangeChunks in the list are affected by the IFileChangeChunk that gets inserted
     *
     * @param toInsert the IFileChangeChunk to apply to the list of IFileChangeChunks
     * @param fileChangeChunkList List of IFileChangeChunks on which to apply toInsert
     * @return List of Integers with the indices of the affected IFileChangeChunks in the list
     */
    @NotNull
    static List<Integer> _affectedChunkIndizes(@NotNull IFileChangeChunk toInsert, @NotNull List<IFileChangeChunk> fileChangeChunkList) {
        List<Integer> affectedChunks = new ArrayList<>();
        int intersectionIndex = 0;
        // Side A ist assumed to be the text from the fork-point
        while (fileChangeChunkList.get(intersectionIndex).getAEnd() < toInsert.getAStart() && intersectionIndex < fileChangeChunkList.size()) {
            intersectionIndex++;
        }
        // all chunks before the affected area are now excluded
        while (fileChangeChunkList.get(intersectionIndex).getAStart() <= toInsert.getAEnd() && intersectionIndex < fileChangeChunkList.size()) {
            affectedChunks.add(intersectionIndex);
            intersectionIndex++;
        }
        return affectedChunks;
    }

    /**
     * Adjust the start/end indices of the IFileChangeChunks in the list, starting from the given index
     *
     * @param fileChangeChunkList the list of IFileChangeChunks
     * @param listIndex the first index that gets the offset
     * @param numLines the number of lines the IFileChangeChunks have been set back
     */
    static void _propagateAdditionalLines(List<IFileChangeChunk> fileChangeChunkList, int listIndex, int numLines) {
        for (int index = listIndex; index < fileChangeChunkList.size(); index++) {
            // FileChangeChunks don't have setters, so create a new one
            IFileChangeChunk updated = new FileChangeChunkImpl(
                    new Edit(fileChangeChunkList.get(index).getAStart() + numLines, fileChangeChunkList.get(index).getAEnd() + numLines,
                            fileChangeChunkList.get(index).getBStart(), fileChangeChunkList.get(index).getBEnd()), fileChangeChunkList.get(index).getALines(), fileChangeChunkList.get(index).getBLines());
            // replace the current FileChangeChunk with the updated one
            fileChangeChunkList.set(index, updated);
        }
    }

}
