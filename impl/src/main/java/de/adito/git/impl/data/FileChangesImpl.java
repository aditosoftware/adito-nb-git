package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChanges;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
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

    private final Subject<List<IFileChangeChunk>> changeChunks;
    private final String[] originalLines;
    private final String[] newLines;

    FileChangesImpl(EditList editList, String pOriginalFileContents, String pNewFileContents) {
        List<IFileChangeChunk> changeChunkList = new ArrayList<>();
        // combine the lineChanges with the information from editList and build chunks
        originalLines = pOriginalFileContents.split("\n", -1);
        newLines = pNewFileContents.split("\n", -1);
        // from beginning of the file to the first chunk
        changeChunkList.add(_getUnchangedChunk(null, editList.get(0)));
        // first chunk extra, since the index in the for loop starts at 0
        changeChunkList.add(_getChangedChunk(editList.get(0)));
        for (int index = 1; index < editList.size(); index++) {
            changeChunkList.add(_getUnchangedChunk(editList.get(index - 1), editList.get(index)));
            changeChunkList.add(_getChangedChunk(editList.get(index)));
        }
        // from last chunk to end of file
        changeChunkList.add(_getUnchangedChunk(editList.get(editList.size() - 1), null));
        changeChunks = BehaviorSubject.createDefault(changeChunkList);
    }

    Subject<List<IFileChangeChunk>> getSubject()
    {
        return changeChunks;
    }

    /**
     * Creates an IFileChangeChunk for the Edit, if one of the sides has more
     * lines than the other, newlines are added to the shorter side so that
     * the sides match up in number of lines
     *
     * @param edit Edit for which a {@link IFileChangeChunk} should be created
     * @return the IFileChangeChunk for the Edit
     */
    private IFileChangeChunk _getChangedChunk(@NotNull Edit edit) {
        StringBuilder oldString = new StringBuilder();
        StringBuilder newString = new StringBuilder();
        StringBuilder oldParityString = new StringBuilder();
        StringBuilder newParityString = new StringBuilder();
        for (int count = 0; count < edit.getEndA() - edit.getBeginA(); count++) {
            oldString.append(originalLines[edit.getBeginA() + count]).append("\n");
        }
        for (int count = 0; count < edit.getEndB() - edit.getBeginB(); count++) {
            newString.append(newLines[edit.getBeginB() + count]).append("\n");
        }
        for (int count = (edit.getEndA() - edit.getBeginA()) - (edit.getEndB() - edit.getBeginB()); count < 0; count++) {
            oldParityString.append("\n");
        }
        for (int count = (edit.getEndB() - edit.getBeginB() - (edit.getEndA() - edit.getBeginA())); count < 0; count++) {
            newParityString.append("\n");
        }
        return new FileChangeChunkImpl(edit, oldString.toString(), newString.toString(), oldParityString.toString(), newParityString.toString());
    }

    /**
     * @param previousEdit {@link Edit} that describes the changes just before the unchanged part. Can be null (if unchanged part is the start of the file)
     * @param nextEdit     {@code Edit} that describes the changes just after the unchanged part. Can be null (if unchanged part is the end of the file)
     * @return {@link IFileChangeChunk} with the lines of the unchanged part between the edits and EChangeType.SAME
     */
    private IFileChangeChunk _getUnchangedChunk(@Nullable Edit previousEdit, @Nullable Edit nextEdit) {
        StringBuilder oldString = new StringBuilder();
        StringBuilder newString = new StringBuilder();
        int aStart, aEnd, bStart, bEnd;
        aStart = aEnd = bStart = bEnd = 0;
        if (previousEdit == null && nextEdit != null) {
            // aStart and bStart already set to 0
            aEnd = nextEdit.getBeginA();
            bEnd = nextEdit.getBeginB();
            for (int index = 0; index < nextEdit.getBeginA(); index++) {
                oldString.append(originalLines[index]).append("\n");
            }
            for (int index = 0; index < nextEdit.getBeginB(); index++) {
                newString.append(newLines[index]).append("\n");
            }
        } else if (previousEdit != null && nextEdit != null) {
            aStart = previousEdit.getEndA();
            bStart = previousEdit.getEndB();
            aEnd = nextEdit.getBeginA();
            bEnd = nextEdit.getBeginB();
            for (int index = previousEdit.getEndA(); index < nextEdit.getBeginA(); index++) {
                oldString.append(originalLines[index]).append("\n");
            }
            for (int index = previousEdit.getEndB(); index < nextEdit.getBeginB(); index++) {
                newString.append(newLines[index]).append("\n");
            }
            // current has to be null here, so not in the parentheses
        } else if (previousEdit != null) {
            aStart = previousEdit.getEndA();
            bStart = previousEdit.getEndB();
            aEnd = originalLines.length;
            if(aEnd == 1 && originalLines[0].equals("")){
                aEnd = 0;
            }
            bEnd = newLines.length;
            for (int index = previousEdit.getEndA(); index < aEnd; index++) {
                oldString.append(originalLines[index]).append("\n");
            }
            for (int index = previousEdit.getEndB(); index < bEnd; index++) {
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
    public Observable<List<IFileChangeChunk>> getChangeChunks() {
        return changeChunks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(IFileChangeChunk current, IFileChangeChunk replaceWith) {
        List<IFileChangeChunk> tmpCopy;
        synchronized (changeChunks) {
            tmpCopy = changeChunks.blockingFirst();
        }
        int currentIndex = tmpCopy.indexOf(current);
        if(currentIndex == -1) {
            return false;
        }
        tmpCopy.set(currentIndex, replaceWith);
        changeChunks.onNext(tmpCopy);
        return true;
    }

}
