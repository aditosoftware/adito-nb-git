package de.adito.git.api.data.diff;

import lombok.NonNull;

import java.io.File;

/**
 * contains a file and the kind of change that happened to it
 *
 * @author m.kaspera 27.09.2018
 */
public interface IFileChangeType
{

  /**
   * returns the file that was changed in any way. Chooses the ChangeSide that is not null, or if both sides are non-null returns EChangeSide.NEW
   *
   * @return File with path starting from the top level directory of the repository
   */
  @NonNull
  File getFile();

  /**
   * returns the path of the file before or after the change, which side is determined by the passed EChangeSide
   *
   * @param pChangeSide EChangeSide that determines if the path of the file before or after the change should be returned
   * @return path of the file before or after the change, null if file does not exist in that point of time
   */
  @NonNull
  File getFile(@NonNull EChangeSide pChangeSide);

  /**
   * @return EChangeType the kind of change that happened to the file
   */
  @NonNull
  EChangeType getChangeType();
}
