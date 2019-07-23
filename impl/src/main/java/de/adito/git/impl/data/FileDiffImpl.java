package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.patch.FileHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Represents information about the uncovered changes by the diff command
 *
 * @author m.kaspera 21.09.2018
 */
public class FileDiffImpl implements IFileDiff
{

  private final DiffEntry diffEntry;
  private final FileHeader fileHeader;
  private FileChangesImpl fileChanges;
  private final File topLevelDirectory;
  private final IFileContentInfo originalFileContent;
  private final IFileContentInfo newFileContent;

  public FileDiffImpl(DiffEntry pDiffEntry, FileHeader pFileHeader, @Nullable File pTopLevelDirectory,
                      IFileContentInfo pOriginalFileContent, IFileContentInfo pNewFileContent)
  {
    diffEntry = pDiffEntry;
    fileHeader = pFileHeader;
    topLevelDirectory = pTopLevelDirectory;
    originalFileContent = pOriginalFileContent;
    newFileContent = pNewFileContent;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId(@NotNull EChangeSide pSide)
  {
    return (pSide == EChangeSide.NEW ? diffEntry.getNewId() : diffEntry.getOldId()).toString();
  }

  @Override
  public @NotNull File getFile()
  {
    String filePath = getAbsoluteFilePath();
    if (filePath == null)
      filePath = getFilePath();
    return new File(filePath);
  }

  @Override
  public @NotNull File getFile(@NotNull EChangeSide pChangeSide)
  {
    return new File(getFilePath(pChangeSide));
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public EChangeType getChangeType()
  {
    return EnumMappings.toEChangeType(diffEntry.getChangeType());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EFileType getFileType(@NotNull EChangeSide pSide)
  {
    return EnumMappings.toEFileType(pSide == EChangeSide.NEW ? diffEntry.getMode(DiffEntry.Side.NEW) : diffEntry.getMode(DiffEntry.Side.OLD));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFilePath(@NotNull EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? diffEntry.getNewPath() : diffEntry.getOldPath();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Charset getEncoding(@NotNull EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? newFileContent.getEncoding().get() : originalFileContent.getEncoding().get();
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
      fileChanges = new FileChangesImpl(edits, originalFileContent.getFileContent(), newFileContent.getFileContent());
    }
    return fileChanges;
  }
}
