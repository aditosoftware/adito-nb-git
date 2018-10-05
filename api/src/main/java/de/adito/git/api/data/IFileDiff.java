package de.adito.git.api.data;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IFileDiff {

    /**
     *
     * @param side {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
     * @return the identifier for the file/object on the specified side of the tree
     */
    String getId(EChangeSide side);

    /**
     *
     * @return {@link EChangeType} that tells which kind of change happened (add/remove...)
     */
    EChangeType getChangeType();

    /**
     *
     * @param side {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
     * @return {@link EFileType} which kind of file
     */
    EFileType getFileType(EChangeSide side);

    /**
     *
     * @param side {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
     * @return the path from root to the file
     */
    String getFilePath(EChangeSide side);

    /**
     *
     * @return {@link IFileChanges} that contains a list detailing the changes in each changed line
     */
    IFileChanges getFileChanges();

}
