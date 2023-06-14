package de.adito.git.api.data;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Collects line annotations for inspection by applications.
 *
 * @author a.arnold, 22.01.2019
 */
public interface IBlame
{

  /**
   * Computes one segment and returns to the caller the first index that is available.
   * After return the caller can also inspect lastLength() to determine how many lines of the result were computed.
   *
   * @return index that is now available. -1 if no more are available.
   */
  int computeNext();

  /**
   * Get the corresponding line number in the source file
   *
   * @param pIndex line to read data of, 0 based
   * @return matching line number in the source file.
   */
  int getSourceLine(int pIndex);

  /**
   * Get the author that provided the specified line of the result
   *
   * @param pIndex line to read data of, 0 based.
   * @return the author of the line
   */
  @Nullable
  String getSourceAuthor(int pIndex);

  /**
   * Get the timestamp of the line
   *
   * @param pIndex the line to get the timestamp
   * @return the timestamp
   */
  @NonNull
  Date getTimeStamp(int pIndex);

  /**
   * Get the commit that provided the specified line of the result
   *
   * @param pIndex line to read data of, 0 based.
   * @return the commit of the line
   */
  @NonNull
  ICommit getSourceCommit(int pIndex);

  /**
   * Get the file path that provided the specified line of the result
   *
   * @param pIndex number of the line
   * @return the path of the file
   */
  @NonNull
  String getSourcePath(int pIndex);

  /**
   * Get the number of lines
   *
   * @return the number of lines
   */
  int getLineCount();
}
