package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

/**
 * Interface for the data object containing the information about the changes to a file
 *
 * @author m.kaspera 20.09.2018
 */
public interface IFileDiff
{

  /**
   * returns the Id for the given side of this IFileDiff
   *
   * @param pSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the identifier for the file/object on the specified side of the tree
   */
  String getId(@NotNull EChangeSide pSide);

  /**
   * @return {@link EChangeType} that tells which kind of change happened (add/remove...)
   */
  EChangeType getChangeType();

  /**
   * returns the type of file for this IFileDiff
   *
   * @param pSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return {@link EFileType} which kind of file
   */
  EFileType getFileType(@NotNull EChangeSide pSide);

  /**
   * returns filePath for the given side
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the path from root to the file
   */
  String getFilePath(@NotNull EChangeSide pChangeSide);

  /**
   * returns the encoding used to convert the fileContents, as String, to a byte array or vice versa
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the encoding used to represent the fileContents as byte array
   */
  Charset getEncoding(@NotNull EChangeSide pChangeSide);

  /**
   * returns the same as getFilePath(EChangeSide.NEW) if file is not deleted,
   * otherwise getFilePath(EChangeSide.OLD) is returned
   *
   * @return path from the root to the file
   */
  String getFilePath();

  /**
   * returns the absolute path of getFilePath(EChangeSide.NEW) if file is not deleted,
   * otherwise the absolute path of getFilePath(EChangeSide.OLD) is returned
   *
   * @return absolute path of the file, or <tt>null</tt> if it can't be determined
   */
  @Nullable
  String getAbsoluteFilePath();

  /**
   * @return {@link IFileChanges} that contains a list detailing the changes in each changed line
   */
  IFileChanges getFileChanges();

}
