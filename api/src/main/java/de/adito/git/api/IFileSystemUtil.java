package de.adito.git.api;

import de.adito.git.api.exception.AditoGitException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Interface for a class that opens a given File in an Editor
 *
 * @author m.kaspera, 18.12.2018
 */
public interface IFileSystemUtil
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


  @NotNull
  Charset getEncoding(@NotNull File pFile);


}
