package de.adito.git.api.data;

import de.adito.git.api.IRepository;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

/**
 * Interface for the data object containing the information about the changes to a file
 *
 * @author m.kaspera 20.09.2018
 */
public interface IFileDiff extends IFileChangeType
{

  /**
   * return an IFileDiffHeader containing information about the id/path/... of the old and new Version of the File as well as the type of change
   *
   * @return IFileDiffHeader of this IFileDiff
   */
  @NotNull
  IFileDiffHeader getFileHeader();

  /**
   * returns the encoding used to convert the fileContents, as String, to a byte array or vice versa
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the encoding used to represent the fileContents as byte array
   */
  Charset getEncoding(@NotNull EChangeSide pChangeSide);

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
  static boolean isSameFile(@NotNull String pFilePath, @NotNull IFileDiff pFileDiff)
  {
    return pFilePath.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW)) || pFilePath.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD));
  }

  /**
   * checks if the IFileDiffs reference the same file, works with renames
   *
   * @param pFileDiff      first IFileDiff
   * @param pOtherFileDiff second IFileDiff
   * @return true if any combination of the paths of the IFileDiffs are the same, except if the matching path is the VOID_PATH. false in all other cases
   */
  static boolean isSameFile(@NotNull IFileDiff pFileDiff, @NotNull IFileDiff pOtherFileDiff)
  {
    if (!IRepository.VOID_PATH.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW))
        && ((pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.NEW)))
        || pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.OLD))))
    {
      return true;
    }
    else return !IRepository.VOID_PATH.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD))
        && (pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.OLD))
        || pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.NEW)));
  }

}
