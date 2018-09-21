package de.adito.git.wrappers;

import org.eclipse.jgit.lib.IndexDiff;

/**
 * @author m.kaspera 20.09.2018
 */
public enum EStageState {

    /**
     * Exists in base, but neither in ours nor in theirs
     */
    BOTH_DELETED(1),

    /**
     * Only exists in ours
     */
    ADDED_BY_US(2),

    /**
     * Exists in base and ours, but no in theirs
     */
    DELETED_BY_THEM(3),

    /**
     * Only exists in theirs
     */
    ADDED_BY_THEM(4),

    /**
     * Exists in base and theirs, but not in ours
     */
    DELETED_BY_US(5),

    /**
     * Exists in ours and theirs, but not in base
     */
    BOTH_ADDED(6),

    /**
     * Exists in all stages, content conflict.
     */
    BOTH_MODIFIED(7);

    private final int stageMask;

    EStageState(int pStageMask) {
        stageMask = pStageMask;
    }

    int getStageMask() {
        return stageMask;
    }

    /**
     * @return whether there is a "base" stage entry
     */
    public boolean hasBase() {
        return (stageMask & 1) != 0;
    }

    /**
     * @return whether there is an "ours" stage entry
     */
    public boolean hasOurs() {
        return (stageMask & 2) != 0;
    }

    /**
     * @return whether there is a "theirs" stage entry
     */
    public boolean hasTheirs() {
        return (stageMask & 4) != 0;
    }

    /**
     *
     * @param stageState IndexDiff.StageState to "wrap"
     * @return "wrapped" IndexDiff.StageState
     */
    public static EStageState fromStageState(IndexDiff.StageState stageState) {
        switch (stageState) {
            case BOTH_DELETED: // 0b001
                return BOTH_DELETED;
            case ADDED_BY_US: // 0b010
                return ADDED_BY_US;
            case DELETED_BY_THEM: // 0b011
                return DELETED_BY_THEM;
            case ADDED_BY_THEM: // 0b100
                return ADDED_BY_THEM;
            case DELETED_BY_US: // 0b101
                return DELETED_BY_US;
            case BOTH_ADDED: // 0b110
                return BOTH_ADDED;
            case BOTH_MODIFIED: // 0b111
                return BOTH_MODIFIED;
            default:
                return null;
        }
    }

}
