package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  public IDeltaTextChangeEvent acceptDelta(IChangeDelta pChangeDelta)
  {
    if (oldVersion == null || newVersion == null)
    {
      reset();
    }
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      String prefix;
      String infix = "";
      int startIndex;
      if (newVersion.length() < pChangeDelta.getStartTextIndex(EChangeSide.NEW))
      {
        prefix = newVersion;
        infix = "\n";
        startIndex = newVersion.length();
      }
      else
      {
        startIndex = Math.max(0, pChangeDelta.getStartTextIndex(EChangeSide.NEW));
        prefix = newVersion.substring(0, startIndex);
      }
      if (pChangeDelta.getChangeStatus().getChangeType() == EChangeType.ADD)
        infix = "";
      else
        infix += oldVersion.substring(pChangeDelta.getStartTextIndex(EChangeSide.OLD), pChangeDelta.getEndTextIndex(EChangeSide.OLD));
      String postFix = newVersion.substring(Math.min(newVersion.length(), pChangeDelta.getEndTextIndex(EChangeSide.NEW)));
      newVersion = prefix + infix + postFix;
      int textDifference = infix.length() - (pChangeDelta.getEndTextIndex(EChangeSide.NEW) - pChangeDelta.getStartTextIndex(EChangeSide.NEW));
      int lineDifference = (pChangeDelta.getEndLine(EChangeSide.OLD) - pChangeDelta.getStartLine(EChangeSide.OLD))
          - (pChangeDelta.getEndLine(EChangeSide.NEW) - pChangeDelta.getStartLine(EChangeSide.NEW));
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      changeDeltas.remove(changeDelta);
      changeDeltas.add(deltaIndex, changeDelta.acceptChange());
      _applyOffsetToFollowingDeltas(deltaIndex, textDifference, lineDifference);
      return new DeltaTextChangeEventImpl(startIndex, (pChangeDelta.getEndTextIndex(EChangeSide.NEW) - pChangeDelta.getStartTextIndex(EChangeSide.NEW)), infix);
    }
    return new DeltaTextChangeEventImpl(0, 0, "");
  }

  @Override
  public void discardDelta(IChangeDelta pChangeDelta)
  {
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      changeDeltas.set(deltaIndex, changeDelta.discardChange());
    }
  }

  @Override
  public void processTextEvent(int pOffset, int pLength, @Nullable String pText)
  {
    if (newVersion == null || oldVersion == null)
      reset();
    String prefix = newVersion.substring(0, pOffset);
    if (pText == null)
    {
      String postfix = newVersion.substring(pOffset + pLength);
      _processDeleteEvent(pOffset, pLength);
      newVersion = prefix + postfix;
    }
    else
    {
      if (pLength > 0)
      {
        String postfix = newVersion.substring(pOffset + pLength);
        _processDeleteEvent(pOffset, pLength);
        newVersion = prefix + postfix;
      }
      String postfix = newVersion.substring(pOffset);
      _processInsertEvent(pOffset, pText);
      newVersion = prefix + pText + postfix;
    }
  }

  /**
   * @param pOffset index of the insertion event
   * @param pText   text that was inserted
   */
  private void _processInsertEvent(int pOffset, @NotNull String pText)
  {
    int lineOffset;
    lineOffset = pText.split("\n", -1).length - 1;

    for (int index = 0; index < getChangeDeltas().size(); index++)
    {
      IChangeDelta currentDelta = changeDeltas.get(index);
      if (pOffset < currentDelta.getEndTextIndex(EChangeSide.NEW))
      {
        if (pOffset >= currentDelta.getStartTextIndex(EChangeSide.NEW))
        {
          // see IChangeDelta.processTextEvent case INSERT 3
          changeDeltas.set(index, currentDelta.processTextEvent(pOffset, pText.length(), 0, lineOffset, true));
          _applyOffsetToFollowingDeltas(index, pText.length(), lineOffset);
        }
        else
        {
          // see IChangeDelta.processTextEvent case INSERT 1
          _applyOffsetToFollowingDeltas(index - 1, pText.length(), lineOffset);
        }
        break;
      }
    }
  }

  /**
   * @param pOffset index of the start of the delete event (first deleted character)
   * @param pLength number of deleted characters
   */
  private void _processDeleteEvent(int pOffset, int pLength)
  {
    int lineOffset;
    String infix = newVersion.substring(pOffset, pOffset + pLength);
    lineOffset = -(infix.split("\n", -1).length - 1);
    for (int index = 0; index < getChangeDeltas().size(); index++)
    {
      IChangeDelta currentDelta = changeDeltas.get(index);
      if (pOffset < currentDelta.getEndTextIndex(EChangeSide.NEW))
      {
        // check if the event may affect a delta that comes after this one
        boolean isChangeBiggerThanDelta = pOffset + pLength > currentDelta.getEndTextIndex(EChangeSide.NEW);
        if (pOffset + pLength < currentDelta.getStartTextIndex(EChangeSide.NEW))
        {
          // see IChangeDelta.processTextEvent case DELETE 5
          changeDeltas.set(index, currentDelta.applyOffset(lineOffset, -pLength));
        }
        else
        {
          // part of the delete operation text that is in front of the chunk
          String deletedBefore = newVersion.substring(Math.min(currentDelta.getStartTextIndex(EChangeSide.NEW), pOffset),
                                                      currentDelta.getStartTextIndex(EChangeSide.NEW));
          // part of the delete operation text that is inside the chunk
          String deletedOfChunk = newVersion.substring(Math.max(currentDelta.getStartTextIndex(EChangeSide.NEW), pOffset),
                                                       Math.min(currentDelta.getEndTextIndex(EChangeSide.NEW), pOffset + pLength));
          changeDeltas.set(index, currentDelta.processTextEvent(pOffset, -pLength, -(deletedBefore.split("\n", -1).length - 1),
                                                                -(deletedOfChunk.split("\n", -1).length - 1), false));
        }
        if (!isChangeBiggerThanDelta)
        {
          _applyOffsetToFollowingDeltas(index, -pLength, lineOffset);
          break;
        }
      }
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
   * applies the given text and lineoffsets to the deltas for indices after pDeltaIndex
   *
   * @param pDeltaIndex     index for the list of deltas, given index is exclusive
   * @param pTextDifference offset that will be added to the textOffsets
   * @param pLineDifference offset that will be added to the lineOffsets
   */
  private void _applyOffsetToFollowingDeltas(int pDeltaIndex, int pTextDifference, int pLineDifference)
  {
    for (int index = pDeltaIndex + 1; index < changeDeltas.size(); index++)
    {
      changeDeltas.set(index, changeDeltas.get(index).applyOffset(pLineDifference, pTextDifference));
    }
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
