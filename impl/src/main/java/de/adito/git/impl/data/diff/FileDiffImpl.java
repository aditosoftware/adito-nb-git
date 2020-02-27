package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    changeDeltas = new ArrayList<>();
    List<_LineInfo> oldTextLineInfos = _getLineInfos(originalFileContentInfo.getFileContent().get());
    List<_LineInfo> newTextLineInfos = _getLineInfos(newFileContentInfo.getFileContent().get());

    for (Edit edit : editList)
    {
      int startIndexOld = _getStartIndexSafely(oldTextLineInfos, edit.getBeginA());
      int endIndexOld = oldTextLineInfos.get(Math.min(oldTextLineInfos.size() - 1, Math.max(edit.getBeginA(), edit.getEndA() - 1))).getEndIndex();
      int startIndexNew = _getStartIndexSafely(newTextLineInfos, edit.getBeginB());
      int endIndexNew;
      if (edit.getType() == Edit.Type.DELETE)
        endIndexNew = startIndexNew;
      else
        endIndexNew = newTextLineInfos.get(Math.min(newTextLineInfos.size() - 1, Math.max(edit.getBeginB(), edit.getEndB() - 1))).getEndIndex();
      changeDeltas.add(new ChangeDeltaImpl(edit, new ChangeStatusImpl(EChangeStatus.PEDNING, EnumMappings.toChangeType(edit.getType())),
                                           startIndexOld, endIndexOld,
                                           startIndexNew, endIndexNew));
    }
  }

  /**
   * @param pLineInfos List of LineInfos
   * @param pIndex     index of the line
   * @return start index of the line, or +1 to the endIndex of the last line if index is out of bounds
   */
  private int _getStartIndexSafely(List<_LineInfo> pLineInfos, int pIndex)
  {
    if (pLineInfos.size() > pIndex)
    {
      return pLineInfos.get(pIndex).getStartIndex();
    }
    else
    {
      // E.g. if there was no \n in the last line, we end up here. Take the last lineInfo and then add one character
      return pLineInfos.get(pLineInfos.size() - 1).getEndIndex() + 1;
    }
  }

  private List<_LineInfo> _getLineInfos(String pText)
  {
    List<_LineInfo> lineInfos = new ArrayList<>();
    int startIndex = 0;
    for (String line : pText.split("\n", -1))
    {
      // + 1 because of the missing \n in line (that is actually there)
      lineInfos.add(new _LineInfo(startIndex, startIndex + line.length() + 1));
      startIndex += line.length() + 1;
    }
    _LineInfo lastLineInfo = lineInfos.get(lineInfos.size() - 1);
    lineInfos.set(lineInfos.size() - 1, new _LineInfo(lastLineInfo.getStartIndex(), lastLineInfo.getEndIndex() - 1));
    return lineInfos;
  }

  /**
   * Pair of two integers for the first and last index of a line
   */
  private static class _LineInfo
  {

    private final int startIndex;
    private final int endIndex;

    private _LineInfo(int pStartIndex, int pEndIndex)
    {
      startIndex = pStartIndex;
      endIndex = pEndIndex;
    }

    public int getStartIndex()
    {
      return startIndex;
    }

    public int getEndIndex()
    {
      return endIndex;
    }
  }
}
