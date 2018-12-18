package de.adito.git.api;

import de.adito.git.api.exception.AditoGitException;

import java.io.File;

/**
 * Interface for a class that opens a given File in an Editor
 *
 * @author m.kaspera, 18.12.2018
 */
public interface IFileOpener
{

  /**
   * @param pAbsolutePath absolute path of the file to be opened in an Editor
   * @throws AditoGitException if the fileObject corresponding to the passed parameter can not be found
   */
  void openFile(String pAbsolutePath) throws AditoGitException;

  /**
   * @param pFile File to be opened in an Editor
   * @throws AditoGitException if the fileObject corresponding to the passed parameter can not be found
   */
  void openFile(File pFile) throws AditoGitException;

}
