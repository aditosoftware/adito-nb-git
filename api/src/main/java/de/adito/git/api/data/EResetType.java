package de.adito.git.api.data;

/**
 * Enum for the different types of reset
 *
 * @author m.kaspera 31.10.2018
 */
public enum EResetType {

    /**
     * Only change where the HEAD is pointing, but leave index and working directory as-is
     */
    SOFT,

    /**
     * Change where the HEAD is pointing and reset the index, but leave the working directory as-is
     */
    MIXED,

    /**
     * Change where the HEAD is pointing, reset the index and also reset the working directory.
     * All changes made will be discarded and cannot be retrieved once this command is executed
     */
    HARD
}
