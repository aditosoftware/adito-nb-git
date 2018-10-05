package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import org.eclipse.jgit.diff.Edit;

/**
 * @author m.kaspera 05.10.2018
 */
public class FileChangeChunkImpl implements IFileChangeChunk {

    private Edit edit;
    private EChangeType changeType;
    private String oldString;
    private String newString;

    public FileChangeChunkImpl(Edit pEdit, EChangeType pChangeType, String pOldString, String pNewString){
        edit = pEdit;
        changeType = pChangeType;
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
        return changeType;
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
