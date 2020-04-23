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


  private final IDiffPathInfo diffPathInfo;
  private final IDiffDetails diffDetails;

  public FileDiffHeaderImpl(@NotNull IDiffPathInfo pDiffPathInfo, @NotNull IDiffDetails pDiffDetails)
  {
    diffPathInfo = pDiffPathInfo;
    diffDetails = pDiffDetails;
  }

  public FileDiffHeaderImpl(@NotNull DiffEntry pDiffEntry, @Nullable File pTopLevelDirectory)
  {
    this(new DiffPathInfoImpl(pTopLevelDirectory, pDiffEntry.getOldPath(), pDiffEntry.getNewPath()),
         new DiffDetailsImpl(pDiffEntry.getOldId().toString(), pDiffEntry.getNewId().toString(), EnumMappings.toEChangeType(pDiffEntry.getChangeType()),
                             EnumMappings.toEFileType(pDiffEntry.getOldMode()), EnumMappings.toEFileType(pDiffEntry.getNewMode())));
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
