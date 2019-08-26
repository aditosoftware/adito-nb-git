package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Represents information about the uncovered changes by the diff command
 *
 * @author m.kaspera 21.09.2018
 */
public class FileDiffImpl implements IFileDiff
{

  private final IFileDiffHeader fileDiffHeader;
  private final EditList editList;
  private FileChangesImpl fileChanges;
  private final IFileContentInfo originalFileContent;
  private final IFileContentInfo newFileContent;


  public FileDiffImpl(@NotNull IFileDiffHeader pFileDiffHeader, @NotNull EditList pEditList, @NotNull IFileContentInfo pOriginalFileContent,
                      @NotNull IFileContentInfo pNewFileContent)
  {
    fileDiffHeader = pFileDiffHeader;
    editList = pEditList;
    originalFileContent = pOriginalFileContent;
    newFileContent = pNewFileContent;
  }

  @NotNull
  @Override
  public IFileDiffHeader getFileHeader()
  {
    return fileDiffHeader;
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

  @Override
  public @NotNull EChangeType getChangeType()
  {
    return fileDiffHeader.getChangeType();
  }

  @NotNull
  @Override
  public File getFile()
  {
    String filePath = fileDiffHeader.getAbsoluteFilePath();
    if (filePath == null)
      filePath = fileDiffHeader.getFilePath();
    return new File(filePath);
  }

  @NotNull
  @Override
  public File getFile(@NotNull EChangeSide pChangeSide)
  {
    return new File(fileDiffHeader.getFilePath(pChangeSide));
  }
}
