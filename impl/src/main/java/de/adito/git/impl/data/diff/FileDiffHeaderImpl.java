package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
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


  private final IDiffPathInfo diffPathInfo;
  private final IDiffDetails diffDetails;
  private final ELineEnding lineEnding;

  public FileDiffHeaderImpl(@NotNull IDiffPathInfo pDiffPathInfo, @NotNull IDiffDetails pDiffDetails, @NotNull ELineEnding pLineEnding)
  {
    diffPathInfo = pDiffPathInfo;
    diffDetails = pDiffDetails;
    lineEnding = pLineEnding;
  }

  public FileDiffHeaderImpl(@NotNull DiffEntry pDiffEntry, @Nullable File pTopLevelDirectory, @NotNull ELineEnding pLineEnding)
  {
    this(new DiffPathInfoImpl(pTopLevelDirectory, pDiffEntry.getOldPath(), pDiffEntry.getNewPath()),
         new DiffDetailsImpl(pDiffEntry.getOldId().toString(), pDiffEntry.getNewId().toString(), EnumMappings.toEChangeType(pDiffEntry.getChangeType()),
                             EnumMappings.toEFileType(pDiffEntry.getOldMode()), EnumMappings.toEFileType(pDiffEntry.getNewMode())),
         pLineEnding);
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public String getId(@NotNull EChangeSide pSide)
  {
    return diffDetails.getId(pSide);
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public EChangeType getChangeType()
  {
    return diffDetails.getChangeType();
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public EFileType getFileType(@NotNull EChangeSide pSide)
  {
    return diffDetails.getFileType(pSide);
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public String getFilePath(@NotNull EChangeSide pSide)
  {
    return diffPathInfo.getFilePath(pSide);
  }

  @NotNull
  @Override
  public String getFilePath()
  {
    if (diffDetails.getChangeType() == EChangeType.DELETE)
      return getFilePath(EChangeSide.OLD);
    return getFilePath(EChangeSide.NEW);
  }

  @Nullable
  @Override
  public String getAbsoluteFilePath()
  {
    if (diffPathInfo.getTopLevelDirectory() == null)
      return null;

    String path = getFilePath();
    return new File(diffPathInfo.getTopLevelDirectory(), path).toPath().toAbsolutePath().toString();
  }

}
