package de.adito.git.api.data;

/**
 * @author m.kaspera 18.10.2018
 */
public interface IMergeDiff {

    /**
     *
     * @return IFileDiff for the comparison current Branch to fork-point commit
     */
    IFileDiff getBaseSideDiff();

    /**
     *
     * @return IFileDiff for the comparison to-merge Branch to fork-point commit
     */
    IFileDiff getMergeSideDiff();

    /**
     *
     * @param toInsert accepts a change from the current branch and adds it to the fork-point commit text
     */
    void insertBaseSideChunk(IFileChangeChunk toInsert);

    /**
     *
     * @param toInsert accepts a change from the to-merge branch and adds it to the fork-point commit text
     */
    void insertMergeSideChunk(IFileChangeChunk toInsert);

    /**
     *
     * @param text inserts a line of text to the fork-point commit text
     */
    void insertText(String text);

}
