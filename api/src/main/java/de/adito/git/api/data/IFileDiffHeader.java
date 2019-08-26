package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines methods that the implementation of an Object that describes meta-informations about the file of a IFileDiff should have
 *
 * @author m.kaspera, 23.08.2019
 */
public interface IFileDiffHeader
{

  /**
   * returns the Id for the given side of this IFileDiff
   *
   * @param pSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the identifier for the file/object on the specified side of the tree
   */
  @NotNull
  String getId(@NotNull EChangeSide pSide);

  /**
   * @return {@link EChangeType} that tells which kind of change happened (add/remove...)
   */
  @NotNull
  EChangeType getChangeType();

  /**
   * returns the type of file for this IFileDiff
   *
   * @param pSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return {@link EFileType} which kind of file
   */
  @NotNull
  EFileType getFileType(@NotNull EChangeSide pSide);

  /**
   * returns filePath for the given side
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the path from root to the file
   */
  String getFilePath(@NotNull EChangeSide pChangeSide);

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

}
