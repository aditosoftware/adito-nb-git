package de.adito.git.api.data;

/**
 * Enum to check the different ref types.
 * There are two type: "heads" and "remotes"
 * "heads" define all of the local branches in the repository
 * "remotes" define all of the remote branches in the repository
 *
 * @author A.Arnold 01.10.2018
 */
public enum EBranchType {
    REMOTE("Remote"),
    LOCAL("Local"),
    DETACHED("Detached"),
    EMPTY("Empty");

    private String displayName;

    /**
     *
     * @param pDisplayName set the ref-type. There are two ref-types: "heads" and "remotes"
     */
    EBranchType(String pDisplayName) {

        displayName = pDisplayName;
    }

    /**
     * @return return the Display Name of the ref
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return return the Enum-Type of the Ref
     */
    public String getSortKey() {
        switch (this) {
            case LOCAL:
                return "heads";
            case REMOTE:
                return "remotes";
            case DETACHED:
                return "detached";
            default:
                return "heads";
        }
    }
}
