package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChanges;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Object that stores the information about which changes happened to a file
 * in IFileChangeChunks
 *
 * @author m.kaspera 24.09.2018
 */
public class FileChangesImpl implements IFileChanges {

    private List<IFileChangeChunk> changeChunks;
    private final String[] originalLines;
    private final String[] newLines;

    FileChangesImpl(EditList editList, String pOriginalFileContents, String pNewFileContents) {
        changeChunks = new ArrayList<>();
        // combine the lineChanges with the information from editList and build chunks
        originalLines = pOriginalFileContents.split("\n");
        newLines = pNewFileContents.split("\n");
        // from beginning of the file to the first chunk
        changeChunks.add(_getUnchangedChunk(null, editList.get(0)));
        // first chunk extra, since the index in the for loop starts at 0
        changeChunks.add(_getChangedChunk(editList.get(0)));
        for(int index = 1; index < editList.size(); index++){
            changeChunks.add(_getUnchangedChunk(editList.get(index - 1), editList.get(index)));
            changeChunks.add(_getChangedChunk(editList.get(index)));
        }
        // from last chunk to end of file
        changeChunks.add(_getUnchangedChunk(editList.get(editList.size() - 1), null));
    }

    /**
     * Creates an IFileChangeChunk for the Edit, if one of the sides has more
     * lines than the other, newlines are added to the shorter side so that
     * the sides match up in number of lines
     *
     * @param edit Edit for which a {@link IFileChangeChunk} should be created
     * @return the IFileChangeChunk for the Edit
     */
    private IFileChangeChunk _getChangedChunk(@NotNull Edit edit){
        StringBuilder oldString = new StringBuilder();
        StringBuilder newString = new StringBuilder();
        for (int count = 0; count < edit.getEndA() - edit.getBeginA(); count++) {
            oldString.append(originalLines[edit.getBeginA() + count]).append("\n");
        }
        for (int count = 0; count < edit.getEndB() - edit.getBeginB(); count++) {
            newString.append(newLines[edit.getBeginB() + count]).append("\n");
        }
        for(int count = (edit.getEndA() - edit.getBeginA()) - (edit.getEndB() - edit.getBeginB()); count < 0; count++){
            oldString.append("\n");
        }
        for(int count = (edit.getEndB() - edit.getBeginB() - (edit.getEndA() - edit.getBeginA())); count < 0; count++){
            newString.append("\n");
        }
        return new FileChangeChunkImpl(edit, oldString.toString(), newString.toString());
    }

    /**
     *
     *
     * @param previousEdit {@link Edit} that describes the changes just before the unchanged part. Can be null (if unchanged part is the start of the file)
     * @param nextEdit {@code Edit} that describes the changes just after the unchanged part. Can be null (if unchanged part is the end of the file)
     * @return {@link IFileChangeChunk} with the lines of the unchanged part between the edits and EChangeType.SAME
     */
    private IFileChangeChunk _getUnchangedChunk(@Nullable Edit previousEdit, @Nullable Edit nextEdit){
        StringBuilder oldString = new StringBuilder();
        StringBuilder newString = new StringBuilder();
        int aStart, aEnd, bStart, bEnd;
        aStart = aEnd = bStart = bEnd = 0;
        if(previousEdit == null && nextEdit != null){
            // aStart and bStart already set to 0
            aEnd = nextEdit.getBeginA();
            bEnd = nextEdit.getBeginB();
            for(int index = 0; index < nextEdit.getBeginA(); index++){
                oldString.append(originalLines[index]).append("\n");
            }
            for(int index = 0; index < nextEdit.getBeginB(); index++){
                newString.append(newLines[index]).append("\n");
            }
        } else if (previousEdit != null && nextEdit != null){
            aStart = previousEdit.getEndA();
            bStart = previousEdit.getEndB();
            aEnd = nextEdit.getBeginA();
            bEnd = nextEdit.getBeginB();
            for(int index = previousEdit.getEndA(); index < nextEdit.getBeginA(); index++){
                oldString.append(originalLines[index]).append("\n");
            }
            for(int index = previousEdit.getEndB(); index < nextEdit.getBeginB(); index++){
                newString.append(newLines[index]).append("\n");
            }
            // current has to be null here, so not in the parentheses
        } else if(previousEdit != null){
            aStart = previousEdit.getEndA();
            bStart = previousEdit.getEndB();
            aEnd = originalLines.length;
            bEnd = newLines.length;
            for(int index = previousEdit.getEndA(); index < originalLines.length; index++){
                oldString.append(originalLines[index]).append("\n");
            }
            for(int index = previousEdit.getEndB(); index < newLines.length; index++){
                newString.append(newLines[index]).append("\n");
            }
        }
        Edit currentEdit = new Edit(aStart, aEnd, bStart, bEnd);
        return new FileChangeChunkImpl(currentEdit, oldString.toString(), newString.toString(), EChangeType.SAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IFileChangeChunk> getChangeChunks() {
        return changeChunks;
    }

}
