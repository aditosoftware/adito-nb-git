package de.adito.git.impl.data;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import lombok.NonNull;

import java.io.File;
import java.util.Objects;

/**
 * @author m.kaspera 27.09.2018
 */
public class FileChangeTypeImpl implements IFileChangeType
{
  private final File file;
  private final File fileBefore;
  private final EChangeType changeType;

  /**
   * @param pFile        File for which the statusChange is recorded, path of the file is from top-level directory of the repo to the file itself
   * @param pEChangeType Type of change that happened to the file
   */
  public FileChangeTypeImpl(@NonNull File pFile, @NonNull File pFileBefore, @NonNull EChangeType pEChangeType)
  {
    file = pFile;
    fileBefore = pFileBefore;
    changeType = pEChangeType;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public File getFile()
  {
    if (changeType == EChangeType.DELETE || changeType == EChangeType.MISSING)
      return fileBefore;
    else
      return file;
  }

  @Override
  public @NonNull File getFile(@NonNull EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? file : fileBefore;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public EChangeType getChangeType()
  {
    return changeType;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    FileChangeTypeImpl that = (FileChangeTypeImpl) pO;
    return Objects.equals(file, that.file) &&
        changeType == that.changeType;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(file, fileBefore, changeType);
  }
}
