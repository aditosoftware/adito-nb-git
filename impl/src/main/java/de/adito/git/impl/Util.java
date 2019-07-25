package de.adito.git.impl;

import de.adito.git.api.IFileSystemUtil;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;

/**
 * @author a.arnold
 */
public class Util
{
  private Util()
  {
  }

  /**
   * Checking the directory is empty and existing.
   *
   * @param pFile The directory to check
   * @return true - The directory is empty. false - the directory isn't empty, or pFile is not a directory
   */
  static boolean isDirEmpty(@NotNull File pFile)
  {
    if (pFile.exists())
    {
      String[] containedFiles = pFile.list();
      if (containedFiles == null)
        return false;
      return containedFiles.length == 0;
    }
    return true;
  }

  /**
   * Get the relative path of a file.
   *
   * @param pFile the file where the dir is needed
   * @param pGit  the git for checking the base dir
   * @return gives the dir from file
   */
  static String getRelativePath(@NotNull File pFile, @NotNull Git pGit)
  {
    String base = pGit.getRepository().getDirectory().getParent();
    String path = pFile.getAbsolutePath();
    return new File(base).toURI().relativize(new File(path).toURI()).getPath();
  }

  /**
   * Attempts to determine the encoding for a byte array that represents a String
   *
   * @param pContents       the byte array
   * @param pFileSystemUtil IFileSystemUtil used to determine the encoding
   * @return the most likely encoding used to represent the byte array as String
   */
  @NotNull
  public static Charset getEncoding(@NotNull byte[] pContents, @NotNull IFileSystemUtil pFileSystemUtil)
  {
    return pFileSystemUtil.getEncoding(pContents);
  }
}
