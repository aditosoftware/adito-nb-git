package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for IFileChanges notification events. Contains a flag
 * that tells the receiver if UI elements should be updated or not
 * and the new value
 *
 * @author m.kaspera, 19.12.2018
 */
public interface IFileChangesEvent
{

  /**
   * Flag that shows if an UI update is necessary or not
   *
   * @return true if the UI should be updated, false otherwise
   */
  boolean isUpdateUI();

  /**
   * returns a list with the current version of the IFileChangeChunks
   *
   * @return List with the updated IFileChangeChunks
   */
  @NotNull
  List<IFileChangeChunk> getNewValue();

  /**
   * @return IEditorChangeEvent describing the change that has to happen in the editor to reflect the current state of the IFileChangeChunks
   */
  @Nullable
  IEditorChangeEvent getEditorChange();
}
