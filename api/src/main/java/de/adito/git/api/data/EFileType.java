package de.adito.git.api.data;

/**
 * Enum that describes the different types of files recognized by GIT
 *
 * @author m.kaspera 24.09.2018
 */
public enum EFileType {

    /**
     * Entry is a tree (a directory)
     */
    TREE,

    /**
     * Entry is a symbolic link
     */
    SYMLINK,

    /**
     * Entry is a non-executable file
     */
    FILE,

    /**
     * Entry is an executable file
     */
    EXECUTABLE_FILE,

    /**
     * Entry is a submodule commit in another repository
     */
    GITLINK,

    /**
     * Entry is missing during Treewalks
     */
    MISSING

}
