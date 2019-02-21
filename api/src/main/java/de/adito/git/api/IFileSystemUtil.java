package de.adito.git.api;

import de.adito.git.api.exception.AditoGitException;
import org.jetbrains.annotations.NotNull;

import java.awt.Image;
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
   * tells netbeans to open the file with the specified absolute path in an editor
   *
   * @param pAbsolutePath absolute path of the file to be opened in an Editor
   * @throws AditoGitException if the fileObject corresponding to the passed parameter can not be found
   */
  void openFile(String pAbsolutePath) throws AditoGitException;

  /**
   * tells netbeans to open the specified file in an editor
   *
   * @param pFile File to be opened in an Editor
   * @throws AditoGitException if the fileObject corresponding to the passed parameter can not be found
   */
  void openFile(File pFile) throws AditoGitException;

  /**
   * find an icon representing the passed file
   *
   * @param pFile     File to find an icon for
   * @param pIsOpened whether the icon should represent an opened or closed state of the file, for example for a node in a tree
   * @return Image depicting the folder/file type
   * @throws AditoGitException if the data object for the file cannot be found
   */
  Image getIcon(File pFile, boolean pIsOpened) throws AditoGitException;


  /**
   * tries to get the correct encoding for the given file
   *
   * @param pFile File
   * @return Charset for the passed file
   */
  @NotNull
  Charset getEncoding(@NotNull File pFile);

}
