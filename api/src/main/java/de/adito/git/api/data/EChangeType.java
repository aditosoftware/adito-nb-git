package de.adito.git.api.data;

import de.adito.git.api.ColorPicker;
import org.jetbrains.annotations.Nullable;

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
    ADD(ColorPicker.VERSIONING_ADDED, ColorPicker.DIFF_ADDED),

    /**
     * new file in the project, file is not yet staged
     */
    NEW(ColorPicker.VERSIONING_ADDED, ColorPicker.DIFF_ADDED),

    /**
     * Modify an existing file in the project (content and/or mode), changes are staged
     */
    CHANGED(ColorPicker.VERSIONING_MODIFIED, ColorPicker.DIFF_MODIFIED),

    /**
     * Modify an existing file in the project (content and/or mode), changes not in the index
     */
    MODIFY(ColorPicker.VERSIONING_MODIFIED, ColorPicker.DIFF_MODIFIED),

    /**
     * Delete an existing file from the project
     */
    DELETE(ColorPicker.VERSIONING_DELETED, ColorPicker.DIFF_DELETED),

    /**
     * File is in index but not on the local disk
     */
    MISSING(ColorPicker.VERSIONING_DELETED, ColorPicker.DIFF_DELETED),

    /**
     * Rename an existing file to a new location
     */
    RENAME(ColorPicker.VERSIONING_MODIFIED, ColorPicker.DIFF_MODIFIED),

    /**
     * Copy an existing file to a new location, keeping the original
     */
    COPY(ColorPicker.VERSIONING_ADDED, ColorPicker.DIFF_ADDED),

    /**
     * file is in a conflicting state towards (e.g what you get if you modify file that was modified by someone else in the meantime)
     */
    CONFLICTING(ColorPicker.VERSIONING_CONFLICTING, ColorPicker.DIFF_UNRESOLVED),

    /**
     * Stayed the same, only used for LineChanges
     */
    SAME(null, null);

    Color statusColor;
    Color diffColor;

    EChangeType(@Nullable Color pStatusColor, @Nullable Color pDiffColor) {
        statusColor = pStatusColor;
        diffColor = pDiffColor;
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
