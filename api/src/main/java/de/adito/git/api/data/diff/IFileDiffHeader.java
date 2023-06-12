package de.adito.git.api.data.diff;

import de.adito.git.api.data.EFileType;
import lombok.NonNull;
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
  @NonNull
  String getId(@NonNull EChangeSide pSide);

  /**
   * @return {@link EChangeType} that tells which kind of change happened (add/remove...)
   */
  @NonNull
  EChangeType getChangeType();

  /**
   * returns the type of file for this IFileDiff
   *
   * @param pSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return {@link EFileType} which kind of file
   */
  @NonNull
  EFileType getFileType(@NonNull EChangeSide pSide);

  /**
   * returns filePath for the given side
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the path from root to the file
   */
  String getFilePath(@NonNull EChangeSide pChangeSide);

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
   * Determine the file extension by parsing the path on the given side
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return File extension of the file on the specified side
   */
  @Nullable String getFileExtension(@NonNull EChangeSide pChangeSide);

  IFileDiffHeader EMPTY_HEADER = new IFileDiffHeader()
  {
    @Override
    public @NonNull String getId(@NonNull EChangeSide pSide)
    {
      return "";
    }

    @Override
    public @NonNull EChangeType getChangeType()
    {
      return EChangeType.MODIFY;
    }

    @Override
    public @NonNull EFileType getFileType(@NonNull EChangeSide pSide)
    {
      return EFileType.FILE;
    }

    @Override
    public String getFilePath(@NonNull EChangeSide pChangeSide)
    {
      return "";
    }

    @Override
    public String getFilePath()
    {
      return "";
    }

    @Override
    public @Nullable String getAbsoluteFilePath()
    {
      return null;
    }

    @Override
    public @Nullable String getFileExtension(@NonNull EChangeSide pChangeSide)
    {
      return null;
    }
  };

}
