package de.adito.git.api.data;

/**
 * Enum that describes the type of change that happened to a file
 *
 * @author m.kaspera 24.09.2018
 */
public enum EChangeType {
    /** Add a new file to the project, already in the index */
    ADD,

    /** new file in the project, file is not yet staged */
    NEW,

    /** Modify an existing file in the project (content and/or mode), changes are staged */
    CHANGED,

    /** Modify an existing file in the project (content and/or mode), changes not in the index */
    MODIFY,

    /** Delete an existing file from the project */
    DELETE,

    /** File is in index but not on the local disk */
    MISSING,

    /** Rename an existing file to a new location */
    RENAME,

    /** Copy an existing file to a new location, keeping the original */
    COPY,

    /** file is in a conflicting state towards (e.g what you get if you modify file that was modified by someone else in the meantime) */
    CONFLICTING,

    /** Stayed the same, only used for LineChanges */
    SAME
}
