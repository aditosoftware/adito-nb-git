package de.adito.git.api.data;

import java.util.List;

/**
 * @author m.kaspera 25.09.2018
 */
public interface IFileChanges {

    /**
     *
     * @return List with the changes made to the file, organized in chunks
     */
    List<IFileChangeChunk> getChangeChunks();

}
