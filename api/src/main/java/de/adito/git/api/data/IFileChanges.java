package de.adito.git.api.data;

import io.reactivex.Observable;

import java.util.List;

/**
 * @author m.kaspera 25.09.2018
 */
public interface IFileChanges {

    /**
     *
     * @return List with the changes made to the file, organized in chunks
     */
    Observable<List<IFileChangeChunk>> getChangeChunks();

    boolean replace(IFileChangeChunk current, IFileChangeChunk replaceWith);
}
