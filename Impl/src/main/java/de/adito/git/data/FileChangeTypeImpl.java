package de.adito.git.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;

import java.io.File;

/**
 * @author m.kaspera 27.09.2018
 */
public class FileChangeTypeImpl implements IFileChangeType {

    private File file;
    private EChangeType changeType;

    public FileChangeTypeImpl(File pFile, EChangeType pEChangeType){
        file = pFile;
        changeType = pEChangeType;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public EChangeType getChangeType() {
        return changeType;
    }
}
