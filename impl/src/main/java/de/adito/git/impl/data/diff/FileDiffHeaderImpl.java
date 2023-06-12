package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileDiffHeader;
import de.adito.git.impl.EnumMappings;
import lombok.NonNull;
import org.eclipse.jgit.diff.DiffEntry;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author m.kaspera, 23.08.2019
 */
public class FileDiffHeaderImpl implements IFileDiffHeader
{


  private final IDiffPathInfo diffPathInfo;
  private final IDiffDetails diffDetails;

  public FileDiffHeaderImpl(@NonNull IDiffPathInfo pDiffPathInfo, @NonNull IDiffDetails pDiffDetails)
  {
    diffPathInfo = pDiffPathInfo;
    diffDetails = pDiffDetails;
  }

  public FileDiffHeaderImpl(@NonNull DiffEntry pDiffEntry, @Nullable File pTopLevelDirectory)
  {
    this(new DiffPathInfoImpl(pTopLevelDirectory, pDiffEntry.getOldPath(), pDiffEntry.getNewPath()),
         new DiffDetailsImpl(pDiffEntry.getOldId().toString(), pDiffEntry.getNewId().toString(), EnumMappings.toEChangeType(pDiffEntry.getChangeType()),
                             EnumMappings.toEFileType(pDiffEntry.getOldMode()), EnumMappings.toEFileType(pDiffEntry.getNewMode())));
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getId(@NonNull EChangeSide pSide)
  {
    return diffDetails.getId(pSide);
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public EChangeType getChangeType()
  {
    return diffDetails.getChangeType();
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public EFileType getFileType(@NonNull EChangeSide pSide)
  {
    return diffDetails.getFileType(pSide);
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getFilePath(@NonNull EChangeSide pSide)
  {
    return diffPathInfo.getFilePath(pSide);
  }

  @NonNull
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

  @Nullable
  @Override
  public String getFileExtension(@NonNull EChangeSide pChangeSide)
  {
    String[] nameParts = Paths.get(getFilePath(pChangeSide)).getFileName().toString().split("[.]");
    if (nameParts.length <= 1)
      return null;
    else
      return nameParts[nameParts.length - 1];
  }

}
