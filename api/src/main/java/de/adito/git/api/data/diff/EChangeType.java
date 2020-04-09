package de.adito.git.api.data.diff;

import de.adito.git.api.ColorPicker;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Enum that describes the type of change that happened to a file
 *
 * @author m.kaspera 24.09.2018
 */
public enum EChangeType
{

  /**
   * Add a new file to the project, already in the index
   */
  ADD(ColorPicker.VERSIONING_ADDED, ColorPicker.DIFF_ADDED, ColorPicker.DIFF_ADDED_SECONDARY),

  /**
   * new file in the project, file is not yet staged
   */
  NEW(ColorPicker.VERSIONING_ADDED, ColorPicker.DIFF_ADDED, ColorPicker.DIFF_ADDED_SECONDARY),

  /**
   * Modify an existing file in the project (content and/or mode), changes are staged
   */
  CHANGED(ColorPicker.VERSIONING_MODIFIED, ColorPicker.DIFF_MODIFIED, ColorPicker.DIFF_MODIFIED_SECONDARY),

  /**
   * Modify an existing file in the project (content and/or mode), changes not in the index
   */
  MODIFY(ColorPicker.VERSIONING_MODIFIED, ColorPicker.DIFF_MODIFIED, ColorPicker.DIFF_MODIFIED_SECONDARY),

  /**
   * Delete an existing file from the project
   */
  DELETE(ColorPicker.VERSIONING_DELETED, ColorPicker.DIFF_DELETED, ColorPicker.DIFF_DELETED_SECONDARY),

  /**
   * File is in index but not on the local disk
   */
  MISSING(ColorPicker.VERSIONING_DELETED, ColorPicker.DIFF_DELETED, ColorPicker.DIFF_DELETED_SECONDARY),

  /**
   * Rename an existing file to a new location
   */
  RENAME(ColorPicker.VERSIONING_MODIFIED, ColorPicker.DIFF_MODIFIED, ColorPicker.DIFF_MODIFIED_SECONDARY),

  /**
   * Copy an existing file to a new location, keeping the original
   */
  COPY(ColorPicker.VERSIONING_ADDED, ColorPicker.DIFF_ADDED, ColorPicker.DIFF_ADDED_SECONDARY),

  /**
   * file is in a conflicting state towards (e.g what you get if you modify file that was modified by someone else in the meantime)
   */
  CONFLICTING(ColorPicker.VERSIONING_CONFLICTING, ColorPicker.DIFF_CONFLICTING, ColorPicker.DIFF_CONFLICTING_SECONDARY),

  /**
   * Stayed the same, only used for LineChanges
   */
  SAME(null, null, null);

  final Color statusColor;
  final Color diffColor;
  final Color secondaryDiffColor;

  EChangeType(@Nullable Color pStatusColor, @Nullable Color pDiffColor, @Nullable Color pSecondaryDiffColor)
  {
    statusColor = pStatusColor;
    diffColor = pDiffColor;
    secondaryDiffColor = pSecondaryDiffColor;
  }

  @Nullable
  public Color getStatusColor()
  {
    return statusColor;
  }

  @Nullable
  public Color getDiffColor()
  {
    return diffColor;
  }

  public Color getSecondaryDiffColor()
  {
    return secondaryDiffColor;
  }
}
