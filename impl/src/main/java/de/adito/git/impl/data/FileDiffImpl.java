package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
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

  private final EditList editList;
  private FileChangesImpl fileChanges;
  private final File topLevelDirectory;
  private final IFileContentInfo originalFileContent;
  private final IFileContentInfo newFileContent;
  private final String oldId;
  private final String newId;
  private final EChangeType changeType;
  private final EFileType oldFileType;
  private final EFileType newFileType;
  private final String oldFilePath;
  private final String newFilePath;


  public FileDiffImpl(@NotNull DiffEntry pDiffEntry, @NotNull EditList pEditList, @Nullable File pTopLevelDirectory,
                      @NotNull IFileContentInfo pOriginalFileContent, @NotNull IFileContentInfo pNewFileContent)
  {
    oldId = pDiffEntry.getOldId().toString();
    newId = pDiffEntry.getNewId().toString();
    changeType = EnumMappings.toEChangeType(pDiffEntry.getChangeType());
    oldFileType = EnumMappings.toEFileType(pDiffEntry.getOldMode());
    newFileType = EnumMappings.toEFileType(pDiffEntry.getNewMode());
    oldFilePath = pDiffEntry.getOldPath();
    newFilePath = pDiffEntry.getNewPath();
    editList = pEditList;
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
    return pSide == EChangeSide.NEW ? newId : oldId;
  }

  @NotNull
  @Override
  public File getFile()
  {
    String filePath = getAbsoluteFilePath();
    if (filePath == null)
      filePath = getFilePath();
    return new File(filePath);
  }

  @NotNull
  @Override
  public File getFile(@NotNull EChangeSide pChangeSide)
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

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public Charset getEncoding(@NotNull EChangeSide pSide)
  {
    return pSide == EChangeSide.NEW ? newFileContent.getEncoding().get() : originalFileContent.getEncoding().get();
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

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public FileChangesImpl getFileChanges()
  {
    if (fileChanges == null)
    {
      fileChanges = new FileChangesImpl(editList, originalFileContent.getFileContent(), newFileContent.getFileContent());
    }
    return fileChanges;
  }
}
