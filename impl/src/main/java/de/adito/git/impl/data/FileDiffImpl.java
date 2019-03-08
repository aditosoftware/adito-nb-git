package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.patch.FileHeader;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Represents information about the uncovered changes by the diff command
 *
 * @author m.kaspera 21.09.2018
 */
public class FileDiffImpl implements IFileDiff
{

  private DiffEntry diffEntry;
  private FileHeader fileHeader;
  private FileChangesImpl fileChanges;
  private File topLevelDirectory;
  private String originalFileContents;
  private String newFileContents;

  public FileDiffImpl(DiffEntry pDiffEntry, FileHeader pFileHeader, @Nullable File pTopLevelDirectory,
                      String pOriginalFileContents, String pNewFileContents)
  {
    diffEntry = pDiffEntry;
    fileHeader = pFileHeader;
    topLevelDirectory = pTopLevelDirectory;
    originalFileContents = pOriginalFileContents;
    newFileContents = pNewFileContents;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId(EChangeSide pSide)
  {
    return (pSide == EChangeSide.NEW ? diffEntry.getNewId() : diffEntry.getOldId()).toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EChangeType getChangeType()
  {
    return EnumMappings.toEChangeType(diffEntry.getChangeType());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EFileType getFileType(EChangeSide pSide)
  {
    return EnumMappings.toEFileType(pSide == EChangeSide.NEW ? diffEntry.getMode(DiffEntry.Side.NEW) : diffEntry.getMode(DiffEntry.Side.OLD));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFilePath(EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? diffEntry.getNewPath() : diffEntry.getOldPath();
  }

  @Override
  public String getFilePath()
  {
    if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE)
      return getFilePath(EChangeSide.OLD);
    return getFilePath(EChangeSide.NEW);
  }

  @Override
  public String getAbsoluteFilePath()
  {
    if (topLevelDirectory == null)
      return null;

    String path = getFilePath();
    if (path == null)
      return null;

    return new File(topLevelDirectory, path).toPath().toAbsolutePath().toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FileChangesImpl getFileChanges()
  {
    if (fileChanges == null)
    {
      EditList edits = fileHeader.getHunks().get(0).toEditList();
      fileChanges = new FileChangesImpl(edits, originalFileContents, newFileContents);
    }
    return fileChanges;
  }
}
