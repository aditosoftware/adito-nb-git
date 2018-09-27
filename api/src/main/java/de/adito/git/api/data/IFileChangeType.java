package de.adito.git.api.data;

import java.io.File;

/**
 * @author m.kaspera 27.09.2018
 */
public interface IFileChangeType {

    File getFile();

    EChangeType getChangeType();
}
