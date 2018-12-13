package de.adito.git.impl.data;

import de.adito.git.api.data.*;

import java.io.File;
import java.util.Objects;

/**
 * @author m.kaspera 27.09.2018
 */
public class FileChangeTypeImpl implements IFileChangeType
{
  private File file;
  private EChangeType changeType;

  public FileChangeTypeImpl(File pFile, EChangeType pEChangeType)
  {
    file = pFile;
    changeType = pEChangeType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public File getFile()
  {
    return file;
  }

  /**
   * {@inheritDoc}
   */
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
    return Objects.hash(file, changeType);
  }
}
