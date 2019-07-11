package de.adito.git.api.data;

import de.adito.git.api.IRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

/**
 * Interface for the data object containing the information about the changes to a file
 *
 * @author m.kaspera 20.09.2018
 */
public interface IFileDiff extends IFileChangeType
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

  /**
   * checks if the IFileDiff matches the filePath, works with renames
   *
   * @param pFilePath given path to a file
   * @param pFileDiff IFileDiff
   * @return true if the new or the old path of the IFileDiff match the given path, false otherwise
   */
  static boolean _isSameFile(@NotNull String pFilePath, @NotNull IFileDiff pFileDiff)
  {
    return pFilePath.equals(pFileDiff.getFilePath(EChangeSide.NEW)) || pFilePath.equals(pFileDiff.getFilePath(EChangeSide.OLD));
  }

  /**
   * checks if the IFileDiffs reference the same file, works with renames
   *
   * @param pFileDiff      first IFileDiff
   * @param pOtherFileDiff second IFileDiff
   * @return true if any combination of the paths of the IFileDiffs are the same, except if the matching path is the VOID_PATH. false in all other cases
   */
  static boolean _isSameFile(@NotNull IFileDiff pFileDiff, @NotNull IFileDiff pOtherFileDiff)
  {
    if (!IRepository.VOID_PATH.equals(pFileDiff.getFilePath(EChangeSide.NEW))
        && (pFileDiff.getFilePath(EChangeSide.NEW).equals(pOtherFileDiff.getFilePath(EChangeSide.NEW)))
        || pFileDiff.getFilePath(EChangeSide.NEW).equals(pFileDiff.getFilePath(EChangeSide.OLD)))
    {
      return true;
    }
    else return !IRepository.VOID_PATH.equals(pFileDiff.getFilePath(EChangeSide.OLD))
        && (pFileDiff.getFilePath(EChangeSide.OLD).equals(pOtherFileDiff.getFilePath(EChangeSide.OLD))
        || pFileDiff.getFilePath(EChangeSide.OLD).equals(pOtherFileDiff.getFilePath(EChangeSide.NEW)));
  }

}
