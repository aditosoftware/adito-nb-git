package de.adito.git.api;

import de.adito.git.api.data.IFileDiff;
import org.jetbrains.annotations.NotNull;

/**
 * Defines methods for a service that offers the possibility of displaying/calculating the difference between two string or two byte arrays
 *
 * @author m.kaspera, 23.08.2019
 */
public interface IStandAloneDiffProvider
{

  /**
   * Diffs two strings and finds the differences
   *
   * @param pVersion1 String to be compared to pVersion2
   * @param pVersion2 String to be compared to pVersion1
   * @return List of IFileChangeChunks containing the changed lines between the two versions
   */
  IFileDiff diffOffline(@NotNull String pVersion1, @NotNull String pVersion2);

  /**
   * Diffs two byte arrays and finds the differences
   *
   * @param pVersion1 byte array to be compared to pVersion2
   * @param pVersion2 byte array to be compared to pVersion1
   * @return List of IFileChangeChunks containing the changed lines between the two versions
   */
  IFileDiff diffOffline(@NotNull byte[] pVersion1, @NotNull byte[] pVersion2);

}
