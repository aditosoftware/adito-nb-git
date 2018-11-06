package de.adito.git.api.data;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Enum that describes the type of change that happened to a file
 *
 * @author m.kaspera 24.09.2018
 */
public enum EChangeType {

    /**
     * Add a new file to the project, already in the index
     */
    ADD("nb.versioning.added.color", "nb.diff.added.color"),

    /**
     * new file in the project, file is not yet staged
     */
    NEW("nb.versioning.added.color", "nb.diff.added.color"),

    /**
     * Modify an existing file in the project (content and/or mode), changes are staged
     */
    CHANGED("nb.versioning.modified.color", "nb.diff.changed.color"),

    /**
     * Modify an existing file in the project (content and/or mode), changes not in the index
     */
    MODIFY("nb.versioning.modified.color", "nb.diff.changed.color"),

    /**
     * Delete an existing file from the project
     */
    DELETE("nb.versioning.deleted.color", "nb.diff.deleted.color"),

    /**
     * File is in index but not on the local disk
     */
    MISSING("nb.versioning.deleted.color", "nb.diff.deleted.color"),

    /**
     * Rename an existing file to a new location
     */
    RENAME("nb.versioning.modified.color", "nb.diff.changed.color"),

    /**
     * Copy an existing file to a new location, keeping the original
     */
    COPY("nb.versioning.added.color", "nb.diff.added.color"),

    /**
     * file is in a conflicting state towards (e.g what you get if you modify file that was modified by someone else in the meantime)
     */
    CONFLICTING("nb.versioning.conflicted.color", "nb.diff.unresolved.color"),

    /**
     * Stayed the same, only used for LineChanges
     */
    SAME(null, null);

    Color statusColor;
    Color diffColor;

    EChangeType(String pStatusUIManagerKey, String pDiffUIManagerKey) {
        if (pStatusUIManagerKey != null)
            statusColor = UIManager.getColor(pStatusUIManagerKey);
        else
            statusColor = null;
        if (pDiffUIManagerKey != null)
            diffColor = UIManager.getColor(pDiffUIManagerKey);
        else
            diffColor = null;
    }

    @Nullable
    public Color getStatusColor() {
        return statusColor;
    }

    @Nullable
    public Color getDiffColor() {
        return diffColor;
    }
}
