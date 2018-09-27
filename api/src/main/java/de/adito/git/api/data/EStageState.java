package de.adito.git.api.data;

/**
 *
 * Enum that shows the different staging types
 *
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

}
