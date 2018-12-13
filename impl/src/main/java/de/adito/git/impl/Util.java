package de.adito.git.impl;

import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author a.arnold
 */
class Util
{
  private Util()
  {
  }

  /**
   * Checking the directory is empty and existing.
   *
   * @param pFile The directory to check
   * @return true - The directory is empty. false - the directory isn't empty.
   */
  static boolean isDirEmpty(@NotNull File pFile)
  {
    if (pFile.exists())
    {
      return pFile.list().length == 0;
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
}
