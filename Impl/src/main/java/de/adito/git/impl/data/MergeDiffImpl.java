package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of the files in a three-way merger:
 *
 * the A-side in each of the IFileDiffs represents the fork-point state at first,
 * and changes with the changeChunks/changes the user accepts from either side.
 * The A-sides should always be the same in both of the IFileDiffs
 *
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

    @Override
    public IFileDiff getBaseSideDiff() {
        return baseSideDiff;
    }

    @Override
    public IFileDiff getMergeSideDiff() {
        return mergeSideDiff;
    }

    @Override
    public void insertBaseSideChunk(IFileChangeChunk toInsert) {
//        IFileChanges changes = mergeSideDiff.getFileChanges();
//        ((FileChangesImpl) changes).getSubject().onNext();
        _insertChangeChunk(toInsert, mergeSideDiff.getFileChanges().getChangeChunks());
    }

    @Override
    public void insertMergeSideChunk(IFileChangeChunk toInsert) {
        _insertChangeChunk(toInsert, baseSideDiff.getFileChanges().getChangeChunks());
    }

    @Override
    public void insertText(String text) {

    }

    private void _insertChangeChunk(@NotNull IFileChangeChunk toInsert, @NotNull List<IFileChangeChunk> fileChangeChunkList) {
        List<Integer> affectedIndizes = MergeDiffImpl._affectedChunkIndizes(toInsert, fileChangeChunkList);
        for(Integer num: affectedIndizes){
            fileChangeChunkList.set(num, MergeDiffImpl._applyChange(toInsert, fileChangeChunkList.get(num)));
        }
        MergeDiffImpl._propagateAdditionalLines(fileChangeChunkList, affectedIndizes.get(affectedIndizes.size() - 1), (toInsert.getBEnd() - toInsert.getBStart()) - (toInsert.getAEnd() - toInsert.getAStart()));
    }

    @NotNull
    static IFileChangeChunk _applyChange(@NotNull IFileChangeChunk toInsert, @NotNull IFileChangeChunk toChange) {
        String[] toInsertLines = toInsert.getBLines().split("\n");
        String[] toChangeLines = toChange.getALines().split("\n");
        StringBuilder toChangeResult = new StringBuilder();
        // start at -1, since in the end, one line means that aStart and aEnd are the same (with one line added, numLines goes to 0)
        int numLines = -1;
        // Insert the lines that come before the change (if toChange chunk starts before toInsert chunk)
        if(toInsert.getChangeType() == EChangeType.ADD){
            // if the change is an insert the last line has to be added as well
            for (int index = 0; index <= toInsert.getAStart() - toChange.getAStart(); index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        } else {
            for (int index = 0; index < toInsert.getAStart() - toChange.getAStart(); index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        }
        int terminateAt;
        if(toInsert.getChangeType() == EChangeType.MODIFY) {
            // if the replace change started in a previous chunk, start from an offset
            int indexOffset = Math.abs(toInsert.getAStart() - toChange.getAStart());
            //
            terminateAt = toChange.getAEnd() >= toInsert.getAEnd() ? toInsertLines.length - indexOffset : toChange.getAEnd() - toInsert.getAStart();
            if (toChange.getAEnd() == toInsert.getAStart()) {
                terminateAt = 1;
            }
            for (int index = 0; index < terminateAt; index++) {
                toChangeResult.append(toInsertLines[index + indexOffset]).append("\n");
                numLines++;
            }
        }
        else {
            terminateAt = toInsert.getBLines().equals("") ? 0 : toInsertLines.length;
            for (int index = 0; index < terminateAt; index++) {
                toChangeResult.append(toInsertLines[index]).append("\n");
                numLines++;
            }
        }
        if (toChange.getAEnd() > toInsert.getAEnd()) {
            // If the toChange chunk contains lines after the change, append these
            for (int index = toInsert.getAEnd() - toChange.getAStart() + 1; index < toChangeLines.length; index++) {
                toChangeResult.append(toChangeLines[index]).append("\n");
                numLines++;
            }
        }
        return new FileChangeChunkImpl(new Edit(toChange.getAStart(), toChange.getAStart() + numLines, toChange.getBStart(), toChange.getBEnd()), toChangeResult.toString(), toChange.getBLines());
    }

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

    static void _propagateAdditionalLines(List<IFileChangeChunk> fileChangeChunkList, int listIndex, int numLines) {
        for (int index = listIndex; index < fileChangeChunkList.size(); index++) {
            IFileChangeChunk updated = new FileChangeChunkImpl(
                    new Edit(fileChangeChunkList.get(index).getAStart() + numLines, fileChangeChunkList.get(index).getAEnd() + numLines,
                            fileChangeChunkList.get(index).getBStart(), fileChangeChunkList.get(index).getBEnd()), fileChangeChunkList.get(index).getALines(), fileChangeChunkList.get(index).getBLines());
            fileChangeChunkList.set(index, updated);
        }
    }

}
