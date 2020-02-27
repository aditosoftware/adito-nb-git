package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileDiffHeader;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.DiffEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author m.kaspera, 23.08.2019
 */
public class FileDiffHeaderImpl implements IFileDiffHeader
{

  private final File topLevelDirectory;
  private final String oldId;
  private final String newId;
  private final EChangeType changeType;
  private final EFileType oldFileType;
  private final EFileType newFileType;
  private final String oldFilePath;
  private final String newFilePath;

  public FileDiffHeaderImpl(@Nullable File pTopLevelDirectory, @NotNull String pOldId, @NotNull String pNewId, @NotNull EChangeType pChangeType,
                            @NotNull EFileType pOldFileType, @NotNull EFileType pNewFileType, @NotNull String pOldFilePath, @NotNull String pNewFilePath)
  {
    topLevelDirectory = pTopLevelDirectory;
    oldId = pOldId;
    newId = pNewId;
    changeType = pChangeType;
    oldFileType = pOldFileType;
    newFileType = pNewFileType;
    oldFilePath = pOldFilePath;
    newFilePath = pNewFilePath;
  }

  public FileDiffHeaderImpl(@NotNull DiffEntry pDiffEntry, @Nullable File pTopLevelDirectory)
  {
    oldId = pDiffEntry.getOldId().toString();
    newId = pDiffEntry.getNewId().toString();
    //changeType = EnumMappings.toEChangeType(pDiffEntry.getChangeType());
    //TODO revert to above
    changeType = EChangeType.CHANGED;
    oldFileType = EnumMappings.toEFileType(pDiffEntry.getOldMode());
    newFileType = EnumMappings.toEFileType(pDiffEntry.getNewMode());
    oldFilePath = pDiffEntry.getOldPath();
    newFilePath = pDiffEntry.getNewPath();
    topLevelDirectory = pTopLevelDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public String getId(@NotNull EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? newId : oldId;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public EChangeType getChangeType()
  {
    return changeType;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public EFileType getFileType(@NotNull EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? newFileType : oldFileType;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public String getFilePath(@NotNull EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? newFilePath : oldFilePath;
  }

  @NotNull
  @Override
  public String getFilePath()
  {
    if (changeType == EChangeType.DELETE)
      return getFilePath(EChangeSide.OLD);
    return getFilePath(EChangeSide.NEW);
  }

  @Nullable
  @Override
  public String getAbsoluteFilePath()
  {
    if (topLevelDirectory == null)
      return null;

    String path = getFilePath();
    return new File(topLevelDirectory, path).toPath().toAbsolutePath().toString();
  }

}
