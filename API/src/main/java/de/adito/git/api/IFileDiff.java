package de.adito.git.api;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.FileChangeObj;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IFileDiff {

    int getChangeStartLine();

    int getChangeEndLine();

    EChangeType getChangeType();

    EFileType getFileType(EChangeSide side);

    String getFilePath(EChangeSide side);

    FileChangeObj getFileChanges();

}
