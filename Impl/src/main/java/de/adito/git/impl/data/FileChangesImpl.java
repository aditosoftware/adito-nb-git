package de.adito.git.impl.data;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChanges;
import de.adito.git.api.data.ILineChange;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.ArrayList;
import java.util.List;

/**
 * Object to store the LineChanges in {@link LineChangeImpl}
 *
 * @author m.kaspera 24.09.2018
 */
public class FileChangesImpl implements IFileChanges {

    private List<IFileChangeChunk> changeChunks;

    public FileChangesImpl(List<ILineChange> lineChanges, EditList editList) {
        changeChunks = new ArrayList<>();
        int index = 0;
        // combine the lineChanges with the information from editList and build chunks
        for (Edit edit : editList) {
            StringBuilder oldString = new StringBuilder();
            StringBuilder newString = new StringBuilder();
            for (int count = 0; count < edit.getEndA() - edit.getBeginA(); count++) {
                oldString.append(lineChanges.get(index).getLineContent()).append("\n");
                index++;
            }
            for (int count = 0; count < edit.getEndB() - edit.getBeginB(); count++) {
                newString.append(lineChanges.get(index).getLineContent()).append("\n");
                index++;
            }
            changeChunks.add(new FileChangeChunkImpl(edit, oldString.toString(), newString.toString()));
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<IFileChangeChunk> getChangeChunks() {
        return changeChunks;
    }

}
