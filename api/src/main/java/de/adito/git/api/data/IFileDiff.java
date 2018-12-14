package de.adito.git.api.data;

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
  String getId(EChangeSide pSide);

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
  EFileType getFileType(EChangeSide pSide);

  /**
   * returns filePath for the given side
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the path from root to the file
   */
  String getFilePath(EChangeSide pChangeSide);

  /**
   * returns the same as getFilePath(EChangeSide.NEW) if file is not deleted,
   * otherwise getFilePath(EChangeSide.OLD) is returned
   *
   * @return path from the root to the file
   */
  String getFilePath();

  /**
   * @return {@link IFileChanges} that contains a list detailing the changes in each changed line
   */
  IFileChanges getFileChanges();

}
