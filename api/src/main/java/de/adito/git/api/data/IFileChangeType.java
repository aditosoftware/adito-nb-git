package de.adito.git.api.data;

import java.io.File;

/**
 * contains a file and the kind of change that happened to it
 *
 * @author m.kaspera 27.09.2018
 */
public interface IFileChangeType
{

  /**
   * returns the file that was changes in any way
   *
   * @return File with path starting from the top level directory of the repository
   */
  File getFile();

  /**
   * @return EChangeType the kind of change that happened to the file
   */
  EChangeType getChangeType();
}
