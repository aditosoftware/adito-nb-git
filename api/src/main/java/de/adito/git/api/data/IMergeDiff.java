package de.adito.git.api.data;

/**
 * @author m.kaspera 18.10.2018
 */
public interface IMergeDiff {

    /**
     *
     * @param conflictSide CONFLICT_SIDE describing if the diff from base-side or branch-to-merge-side to fork-point is wanted
     * @return IFileDiff for the comparison branch-to-merge to fork-point commit (CONFLICT_SIDE.THEIRS) or base-side to fork-point commit (CONFLICT_SIDE.YOURS)
     */
    IFileDiff getDiff(CONFLICT_SIDE conflictSide);

    /**
     *
     * @param acceptedChunk the change that should be accepted and added to the fork-point commit
     * @param conflictSide CONFLICT_SIDE from which the chunk originates
     */
    void acceptChunk(IFileChangeChunk acceptedChunk, CONFLICT_SIDE conflictSide);

    /**
     *
     * @param acceptedChunk the change that should be discarded
     * @param conflictSide CONFLICT_SIDE from which the chunk originates
     */
    void discardChange(IFileChangeChunk acceptedChunk, CONFLICT_SIDE conflictSide);

    /**
     *
     * @param text inserts a line of text to the fork-point commit text
     */
    void insertText(String text);

    enum CONFLICT_SIDE {YOURS, THEIRS}

}
