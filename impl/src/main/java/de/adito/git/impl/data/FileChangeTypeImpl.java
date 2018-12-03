package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;

import java.io.File;
import java.util.Objects;

/**
 * @author m.kaspera 27.09.2018
 */
public class FileChangeTypeImpl implements IFileChangeType {

    private File file;
    private EChangeType changeType;

    FileChangeTypeImpl(File pFile, EChangeType pEChangeType) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileChangeTypeImpl that = (FileChangeTypeImpl) o;
        return Objects.equals(file, that.file) &&
                changeType == that.changeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, changeType);
    }
}
