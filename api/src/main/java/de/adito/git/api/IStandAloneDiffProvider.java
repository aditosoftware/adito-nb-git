package de.adito.git.api;

import de.adito.git.api.data.diff.IFileDiff;
import lombok.NonNull;

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
  IFileDiff diffOffline(@NonNull String pVersion1, @NonNull String pVersion2);

  /**
   * Diffs two byte arrays and finds the differences
   *
   * @param pVersion1 byte array to be compared to pVersion2
   * @param pVersion2 byte array to be compared to pVersion1
   * @return List of IFileChangeChunks containing the changed lines between the two versions
   */
  IFileDiff diffOffline(@NonNull byte[] pVersion1, @NonNull byte[] pVersion2);

}
