package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author m.kaspera, 24.02.2020
 */
public class FileDiffImpl implements IFileDiff
{

  private final IFileDiffHeader fileDiffHeader;
  private final EditList editList;
  private final IFileContentInfo originalFileContentInfo;
  private final IFileContentInfo newFileContentInfo;
  private String oldVersion;
  private String newVersion;
  private List<IChangeDelta> changeDeltas;

  public FileDiffImpl(@NotNull IFileDiffHeader pFileDiffHeader, @NotNull EditList pEditList, @NotNull IFileContentInfo pOriginalFileContentInfo,
                      @NotNull IFileContentInfo pNewFileContentInfo)
  {
    fileDiffHeader = pFileDiffHeader;
    editList = pEditList;
    originalFileContentInfo = pOriginalFileContentInfo;
    newFileContentInfo = pNewFileContentInfo;
  }

  @Override
  public @NotNull IFileDiffHeader getFileHeader()
  {
    return fileDiffHeader;
  }

  @Override
  public Charset getEncoding(@NotNull EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFileContentInfo.getEncoding().get() : originalFileContentInfo.getEncoding().get();
  }

  @Override
  public ELineEnding getUsedLineEndings(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFileContentInfo.getLineEnding().get() : originalFileContentInfo.getLineEnding().get();
  }

  @Override
  public List<IChangeDelta> getChangeDeltas()
  {
    if (changeDeltas == null)
    {
      _initChangeDeltas();
    }
    return changeDeltas;
  }

  @Override
  public void reset()
  {
    oldVersion = originalFileContentInfo.getFileContent().get();
    newVersion = newFileContentInfo.getFileContent().get();
    _initChangeDeltas();
  }

  @Override
  public void acceptDelta(IChangeDelta pChangeDelta)
  {
    if (oldVersion == null || newVersion == null)
    {
      reset();
    }
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      String prefix;
      if (newVersion.length() < pChangeDelta.getStartTextIndex(EChangeSide.NEW))
        prefix = newVersion + "\n";
      else
        prefix = newVersion.substring(0, Math.max(0, pChangeDelta.getStartTextIndex(EChangeSide.NEW)));
      String infix;
      if (pChangeDelta.getChangeStatus().getChangeType() == EChangeType.ADD)
        infix = "";
      else
        infix = oldVersion.substring(pChangeDelta.getStartTextIndex(EChangeSide.OLD), pChangeDelta.getEndTextIndex(EChangeSide.OLD));
      String postFix = newVersion.substring(Math.min(newVersion.length(), pChangeDelta.getEndTextIndex(EChangeSide.NEW)));
      newVersion = prefix + infix + postFix;
      int textDifference = infix.length() - (pChangeDelta.getEndTextIndex(EChangeSide.NEW) - pChangeDelta.getStartTextIndex(EChangeSide.NEW));
      int lineDifference = (pChangeDelta.getEndLine(EChangeSide.OLD) - pChangeDelta.getStartLine(EChangeSide.OLD))
          - (pChangeDelta.getEndLine(EChangeSide.NEW) - pChangeDelta.getStartLine(EChangeSide.NEW));
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      changeDeltas.remove(changeDelta);
      changeDeltas.add(deltaIndex, changeDelta.acceptChange());
      for (int index = deltaIndex + 1; index < changeDeltas.size(); index++)
      {
        changeDeltas.set(index, changeDeltas.get(index).applyOffset(lineDifference, textDifference));
      }
    }
  }

  @Override
  public void discardDelta(IChangeDelta pChangeDelta)
  {
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      changeDeltas.remove(changeDelta);
      changeDeltas.add(deltaIndex, changeDelta.discardChange());
    }
  }

  @Override
  public String getText(EChangeSide pChangeSide)
  {
    if (oldVersion == null || newVersion == null)
      reset();
    return pChangeSide == EChangeSide.NEW ? newVersion : oldVersion;
  }

  @Override
  public @NotNull File getFile()
  {
    String filePath = fileDiffHeader.getAbsoluteFilePath();
    if (filePath == null)
      filePath = fileDiffHeader.getFilePath();
    return new File(filePath);
  }

  @Override
  public @NotNull File getFile(@NotNull EChangeSide pChangeSide)
  {
    return new File(fileDiffHeader.getFilePath(pChangeSide));
  }

  @Override
  public @NotNull EChangeType getChangeType()
  {
    return fileDiffHeader.getChangeType();
  }

  /**
   * Inits or resets the list of changeDeltas
   */
  private void _initChangeDeltas()
  {
    changeDeltas = LineIndexDiffUtil.getTextOffsets(originalFileContentInfo.getFileContent().get(), newFileContentInfo.getFileContent().get(), editList,
                                                    new ChangeDeltaImplFactory());
  }

  /**
   * Factory, creates ChangeDeltaImpls from an Edit and a ChangeDeltaTextOffsets data object
   */
  private class ChangeDeltaImplFactory implements IChangeDeltaFactory<IChangeDelta>
  {

    @NotNull
    @Override
    public IChangeDelta createDelta(@NotNull Edit pEdit, @NotNull ChangeDeltaTextOffsets pDeltaTextOffsets)
    {
      return new ChangeDeltaImpl(pEdit, new ChangeStatusImpl(EChangeStatus.PEDNING, EnumMappings.toChangeType(pEdit.getType())),
                                 pDeltaTextOffsets, FileDiffImpl.this::getText);
    }
  }

}
