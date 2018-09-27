package de.adito.git.api.data;

import java.io.File;

/**
 * contains a file and the kind of change that happened to it
 *
 * @author m.kaspera 27.09.2018
 */
public interface IFileChangeType {

    /**
     *
     * @return File the File that was changed in any way
     */
    File getFile();

    /**
     *
     * @return EChangeType the kind of change that happened to the file
     */
    EChangeType getChangeType();
}
