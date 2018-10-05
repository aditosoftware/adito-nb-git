package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import org.eclipse.jgit.diff.Edit;

/**
 * @author m.kaspera 05.10.2018
 */
public class FileChangeChunkImpl implements IFileChangeChunk {

    private Edit edit;
    private String oldString;
    private String newString;

    public FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString){
        edit = pEdit;
        oldString = pOldString;
        newString = pNewString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAStart() {
        return edit.getBeginA();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAEnd() {
        return edit.getEndA();
    }

    @Override
    public int getBStart() {
        return edit.getBeginB();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBEnd() {
        return edit.getEndB();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EChangeType getChangeType() {
        switch (edit.getType())
        {
            case REPLACE:
                return EChangeType.MODIFY;
            case INSERT:
                return EChangeType.ADD;
            case DELETE:
                return EChangeType.DELETE;
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getALines() {
        return oldString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBLines() {
        return newString;
    }
}
