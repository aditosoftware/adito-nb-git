package de.adito.git.impl;

import de.adito.git.api.IFileSystemUtil;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
   * Retrieve a value for the key and class from a resource bundle named "Bundle"
   *
   * @param pClass Class, determines the package in which the ResourceBundle searches for the bundle
   * @param pKey   Key that should be used for retrieving the resource from the bundle
   * @return the string for the given key
   * @throws NullPointerException     if <code>pKey</code> or <code>pClass</code> is <code>null</code>
   * @throws MissingResourceException if no object for the given key can be found, or no bundle can be found in the package determined by the class
   * @throws ClassCastException       if the object found for the given key is not a string
   */
  @NotNull
  public static String getResource(@NotNull Class<?> pClass, @NotNull String pKey)
  {
    return getResource(pClass, "Bundle", pKey);
  }

  /**
   * Retrieve a value for the key and class from the given resource bundle
   *
   * @param pClass      class, determines the package in which the ResourceBundle searches for the bundle
   * @param pBundleName Name of the bundle
   * @param pKey        Key that should be used for retrieving the resource from the bundle
   * @return the string for the given key
   * @throws NullPointerException     if <code>pKey</code> or <code>pBundleName</code> is <code>null</code>
   * @throws MissingResourceException if no object for the given key can be found, or if no bundle can be found for the name/package
   * @throws ClassCastException       if the object found for the given key is not a string
   */
  @NotNull
  public static String getResource(@NotNull Class<?> pClass, @NotNull String pBundleName, @NotNull String pKey)
  {
    return ResourceBundle.getBundle(pClass.getPackageName() + "." + pBundleName).getString(pKey);
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

  /**
   * this method performs a safe charAt call (check for length of string and oob stuff) and then checks if the char is equal to any of the passed characters in
   * pCompareTo.
   * If the charAt call would cause an OutOfBoundsException/NullPointerExcption this method just returns false
   *
   * @param pString    String for which a character should be checked
   * @param pIndex     index of the character in the string
   * @param pCompareTo characters to compare the character of the string to
   * @return true only if the character at the given index can be accessed and is equal to any of the given characters, else false
   */
  public static boolean safeIsCharAt(@Nullable String pString, int pIndex, char... pCompareTo)
  {
    return pString != null && pIndex >= 0 && pIndex < pString.length() && _anyMatch(pString.charAt(pIndex), pCompareTo);
  }

  /**
   * check if the given character matches any of the characters in pCompareTo
   *
   * @param pChar      character to compare
   * @param pCompareTo vararg of characters to compare with pChar
   * @return true if any of the characters in pCompareTo match pChar, false otherwise
   */
  private static boolean _anyMatch(char pChar, char... pCompareTo)
  {
    for (char character : pCompareTo)
    {
      if (pChar == character)
        return true;
    }
    return false;
  }
}
