package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 20.02.2020
 */
public final class ChangeDeltaImpl implements IChangeDelta
{

  private final IChangeStatus changeStatus;
  private final int startLineIndexNew;
  private final int endLineIndexNew;
  private final int startTextIndexNew;
  private final int endTextIndexNew;
  private final int startLineIndexOld;
  private final int endLineIndexOld;
  private final int startTextIndexOld;
  private final int endTextIndexOld;
  private List<ILinePartChangeDelta> linePartChangeDeltas;
  private final ITextVersionProvider textVersionProvider;
  private final TextEventIndexUpdater textEventIndexUpdater = new TextEventIndexUpdater();

  public ChangeDeltaImpl(@NotNull Edit pEdit, @NotNull IChangeStatus pChangeStatus, @NotNull ChangeDeltaTextOffsets pChangeDeltaTextOffsets,
                         @NotNull ITextVersionProvider pTextVersionProvider)
  {
    this(pEdit, pChangeStatus, pChangeDeltaTextOffsets, pTextVersionProvider, null);
  }

  private ChangeDeltaImpl(@NotNull Edit pEdit, @NotNull IChangeStatus pChangeStatus, @NotNull ChangeDeltaTextOffsets pChangeDeltaTextOffsets,
                          @NotNull ITextVersionProvider pTextVersionProvider, @Nullable List<ILinePartChangeDelta> pLinePartChangeDeltas)
  {
    changeStatus = pChangeStatus;
    startLineIndexOld = pEdit.getBeginA();
    endLineIndexOld = pEdit.getEndA();
    startLineIndexNew = pEdit.getBeginB();
    endLineIndexNew = pEdit.getEndB();
    startTextIndexOld = pChangeDeltaTextOffsets.getStartIndexOriginal();
    endTextIndexOld = pChangeDeltaTextOffsets.getEndIndexOriginal();
    startTextIndexNew = pChangeDeltaTextOffsets.getStartIndexChanged();
    endTextIndexNew = pChangeDeltaTextOffsets.getEndIndexChanged();
    textVersionProvider = pTextVersionProvider;
    linePartChangeDeltas = pLinePartChangeDeltas;
  }

  public static Edit getLineInfo(IChangeDelta pChangeDelta)
  {
    return new Edit(pChangeDelta.getStartLine(EChangeSide.OLD), pChangeDelta.getEndLine(EChangeSide.OLD),
                    pChangeDelta.getStartLine(EChangeSide.NEW), pChangeDelta.getEndLine(EChangeSide.NEW));
  }

  @Override
  public IChangeStatus getChangeStatus()
  {
    return changeStatus;
  }

  @Override
  public int getStartLine(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? startLineIndexNew : startLineIndexOld;
  }

  @Override
  public int getEndLine(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? endLineIndexNew : endLineIndexOld;
  }

  @Override
  public int getStartTextIndex(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? startTextIndexNew : startTextIndexOld;
  }

  @Override
  public int getEndTextIndex(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? endTextIndexNew : endTextIndexOld;
  }

  @Override
  public IChangeDelta applyOffset(int pLineOffset, int pTextOffset, EChangeSide pChangeSide)
  {
    int lineOffsetOld = pChangeSide == EChangeSide.OLD ? pLineOffset : 0;
    int textOffsetOld = pChangeSide == EChangeSide.OLD ? pTextOffset : 0;
    int lineOffsetNew = pChangeSide == EChangeSide.NEW ? pLineOffset : 0;
    int textOffsetNew = pChangeSide == EChangeSide.NEW ? pTextOffset : 0;
    Edit changedEdit = new Edit(startLineIndexOld + lineOffsetOld, endLineIndexOld + lineOffsetOld, startLineIndexNew + lineOffsetNew, endLineIndexNew + lineOffsetNew);
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(getChangeStatus().getChangeStatus(), getChangeStatus().getChangeType()),
                               new ChangeDeltaTextOffsets(startTextIndexOld + textOffsetOld, endTextIndexOld + textOffsetOld,
                                                          startTextIndexNew + textOffsetNew, endTextIndexNew + textOffsetNew),
                               textVersionProvider,
                               linePartChangeDeltas == null ? null : linePartChangeDeltas.stream()
                                   .map(pILinePartChangeDelta -> pILinePartChangeDelta.applyOffset(pTextOffset, pChangeSide))
                                   .collect(Collectors.toList()));
  }

  @Override
  public IChangeDelta processTextEvent(int pOffset, int pLength, int pNumNewlinesBefore, int pNumNewlines, boolean pIsInsert, EChangeSide pChangeSide)
  {
    Edit modifiedEdit;
    int startTextIndex = pChangeSide == EChangeSide.NEW ? startTextIndexNew : startTextIndexOld;
    int endTextIndex = pChangeSide == EChangeSide.NEW ? endTextIndexNew : endTextIndexOld;
    IChangeStatus modifiedChangeStatus = new ChangeStatusImpl(changeStatus.getChangeStatus(), changeStatus.getChangeType());//EChangeStatus.UNDEFINED
    ChangeDeltaTextOffsets modifiedChangeDeltaOffsets;
    if (pIsInsert)
    {
      modifiedEdit = textEventIndexUpdater.updateLineIndizes(pChangeSide, pStartLineIndex -> pStartLineIndex, pEndLineIndex -> pEndLineIndex + pNumNewlines);
      modifiedChangeDeltaOffsets = textEventIndexUpdater.updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex, pEndTextIndex -> pEndTextIndex + pLength);
    }
    else
    {
      if (pOffset < startTextIndex)
      {
        modifiedEdit = textEventIndexUpdater.updateLineIndizes(pChangeSide, pStartLineIndex -> pStartLineIndex + pNumNewlinesBefore,
                                                               pEndLineIndex -> pEndLineIndex + pNumNewlines + pNumNewlinesBefore);
        if (pOffset + -pLength < endTextIndex)
        {
          // case DELETE 1
          modifiedChangeDeltaOffsets = textEventIndexUpdater.updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex + (pOffset - pStartTextIndex),
                                                                               pEndTextIndex -> pEndTextIndex + pLength);
        }
        else
        {
          // case DELETE 2
          modifiedChangeDeltaOffsets = textEventIndexUpdater.updateTextIndizes(pChangeSide, pStartTextIndex -> pOffset, pEndTextIndex -> pOffset);
        }
      }
      else
      {
        modifiedEdit = textEventIndexUpdater.updateLineIndizes(pChangeSide, pStartLineIndex -> pStartLineIndex, pEndLineIndex -> pEndLineIndex + pNumNewlines);
        if (pOffset + -pLength <= endTextIndex)
        {
          // case DELETE 3/7
          modifiedChangeDeltaOffsets = textEventIndexUpdater.updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex, pEndTextIndex -> pEndTextIndex + pLength);
        }
        else
        {
          // case DELETE 4
          modifiedChangeDeltaOffsets = textEventIndexUpdater.
              updateTextIndizes(pChangeSide, pStartTextIndex -> pStartTextIndex, pEndTextIndex -> pEndTextIndex - (pEndTextIndex - pOffset));
        }
      }
    }
    return new ChangeDeltaImpl(modifiedEdit, modifiedChangeStatus, modifiedChangeDeltaOffsets, textVersionProvider, null);
  }

  @Override
  public List<ILinePartChangeDelta> getLinePartChanges()
  {
    if (linePartChangeDeltas == null)
      linePartChangeDeltas = _calculateLinePartChangeDeltas();
    return linePartChangeDeltas;
  }

  @Override
  public boolean isConflictingWith(IChangeDelta pOtherChangeDelta)
  {
    if (pOtherChangeDelta.getStartTextIndex(EChangeSide.OLD) < endTextIndexOld && pOtherChangeDelta.getEndTextIndex(EChangeSide.OLD) > startTextIndexOld)
    {
      return pOtherChangeDelta.getLinePartChanges()
          .stream().anyMatch(pLinePartChangeDelta -> getLinePartChanges()
              .stream().anyMatch(pOwnLinePartChangeDelta -> pOwnLinePartChangeDelta.isConflictingWith(pLinePartChangeDelta) && !isSameChange(pOtherChangeDelta)));
    }
    else
      return false;
  }

  @Override
  public String getText(EChangeSide pChangeSide)
  {
    if (pChangeSide == EChangeSide.NEW)
    {
      return textVersionProvider.getVersion(pChangeSide).substring(startTextIndexNew, endTextIndexNew);
    }
    else
    {
      return textVersionProvider.getVersion(pChangeSide).substring(startTextIndexOld, endTextIndexOld);
    }
  }

  @Override
  public boolean isSameChange(IChangeDelta pOtherChangeDelta)
  {
    return (startTextIndexOld == pOtherChangeDelta.getStartTextIndex(EChangeSide.OLD) && endTextIndexOld == pOtherChangeDelta.getEndTextIndex(EChangeSide.OLD)
        && getText(EChangeSide.NEW).equals(pOtherChangeDelta.getText(EChangeSide.NEW)));
  }


  private List<ILinePartChangeDelta> _calculateLinePartChangeDeltas()
  {
    String originalVersion;
    String newVersion;
    // if the type is add, make sure the original version contains only empty text
    if (changeStatus.getChangeType() == EChangeType.ADD)
      originalVersion = "";
    else
      // the diff only works on a line-basis, so we split our line such that each word is on a separate line
      originalVersion = textVersionProvider.getVersion(EChangeSide.OLD).substring(startTextIndexOld, endTextIndexOld);
    if (changeStatus.getChangeType() == EChangeType.DELETE)
      newVersion = "";
    else
      newVersion = textVersionProvider.getVersion(EChangeSide.NEW).substring(startTextIndexNew, endTextIndexNew);
    String originalProcessedVersion = originalVersion.replace(" ", "\n");
    String newProcessedVersion = newVersion.replace(" ", "\n");
    EditList editList = LineIndexDiffUtil.getChangedLines(originalProcessedVersion, newProcessedVersion, RawTextComparator.DEFAULT);
    if (changeStatus.getChangeType() == EChangeType.MODIFY)
      editList = _validateLines(editList, originalVersion, newVersion, originalProcessedVersion, newProcessedVersion);
    return LineIndexDiffUtil.getTextOffsets(originalProcessedVersion, newProcessedVersion, editList,
                                            new LinePartChangeDeltaFactory(startTextIndexOld, startTextIndexNew));
  }

  /**
   * Goes through the lines and checks if there are lines the diff algorithm considered equal. The lines may not be equal however, when the "\n" is a "\n" in
   * one line and replaced " " in the other. This methods detects all these cases and modifies the editList in such a way that those lines are also considered changed
   *
   * @param pEditList                EditList
   * @param pOriginalVersion         original version of the string
   * @param pNewVersion              new version of the string
   * @param pOriginalReplacedVersion original version of the string with " " replaced by "\n"
   * @param pNewReplacedVersion      new version of the string with " " replaced by "\n"
   * @return corrected version of the EditList
   */
  static EditList _validateLines(EditList pEditList, String pOriginalVersion, String pNewVersion, String pOriginalReplacedVersion, String pNewReplacedVersion)
  {
    EditList editList = pEditList;
    List<Integer> unmodifiedOriginalLines = _getUnmodifiedLines(editList, pOriginalReplacedVersion, new OriginalEditSideInfo());
    List<Integer> unmodifiedNewLines = _getUnmodifiedLines(editList, pNewReplacedVersion, new NewEditSideInfo());
    List<Integer> checkedNewLines = new ArrayList<>();
    for (int unmodifedLine : unmodifiedOriginalLines)
    {
      int correspondingLine = unmodifedLine + _getOffset(pEditList, unmodifedLine, new OriginalEditSideInfo(), new NewEditSideInfo());
      checkedNewLines.add(correspondingLine);
      if (_isDifferentCharacter(pOriginalVersion, StringUtils.ordinalIndexOf(pOriginalReplacedVersion, "\n", unmodifedLine + 1), pNewVersion,
                                StringUtils.ordinalIndexOf(pNewReplacedVersion, "\n", correspondingLine + 1)))
      {
        _fixEditList(editList, unmodifedLine, correspondingLine);
      }
    }
    unmodifiedNewLines.removeAll(checkedNewLines);
    for (int unmodifedLine : unmodifiedNewLines)
    {
      int correspondingLine = unmodifedLine + _getOffset(pEditList, unmodifedLine, new NewEditSideInfo(), new OriginalEditSideInfo());
      if (_isDifferentCharacter(pOriginalVersion, StringUtils.ordinalIndexOf(pOriginalReplacedVersion, "\n", correspondingLine + 1), pNewVersion,
                                StringUtils.ordinalIndexOf(pNewReplacedVersion, "\n", unmodifedLine + 1)))
      {
        _fixEditList(editList, correspondingLine, unmodifedLine);
      }
    }
    editList = _compressList(editList);
    return editList;
  }

  /**
   * Goes through the editList and gathers all lines of the side implicitly specified by the IEditSideInfo that are not changed, according to the editList
   *
   * @param pEditList        EditList with info about modified lines
   * @param pReplacedVersion version of the String of one side that has its " " replaced by "\n". Used to determine the number of lines in total
   * @param pEditSideInfo    IEditSideInfo the get the start and endIndices for the wanted side
   * @return list of lines that are not changed, according to the editList
   */
  static List<Integer> _getUnmodifiedLines(EditList pEditList, String pReplacedVersion, IEditSideInfo pEditSideInfo)
  {
    List<Integer> unmodifiedLines = new ArrayList<>();
    int numNewlines = StringUtils.countMatches(pReplacedVersion, "\n");
    int index = 0;
    int currentEditIndex = 0;
    while (index < numNewlines)
    {
      Edit currentEdit = pEditList.get(currentEditIndex);
      // index is still smaller than start of the next changed part
      if (index < pEditSideInfo.getStart(currentEdit))
      {
        unmodifiedLines.add(index);
        index++;
      }
      // index is part of the change (represents a modified line)
      else if (index >= pEditSideInfo.getStart(currentEdit) && index < pEditSideInfo.getEnd(currentEdit))
      {
        index++;
      }
      else
      {
        if (currentEditIndex + 1 < pEditList.size())
        {
          if (_isPointChange(pEditSideInfo, currentEdit) && pEditSideInfo.getEnd(currentEdit) == index)
            index++;
          currentEditIndex++;
        }
        else
        {
          if (!_isPointChange(pEditSideInfo, currentEdit))
            unmodifiedLines.add(index);
          index++;
        }
      }
    }
    return unmodifiedLines;
  }

  /**
   * check if the change is an ADD or REMOVE change (start and endIndex of one side is equal)
   *
   * @param pEditSideInfo which side to check
   * @param pEdit         the Edit
   * @return true if the start and endIndex of the side to check are equal
   */
  private static boolean _isPointChange(IEditSideInfo pEditSideInfo, Edit pEdit)
  {
    return pEditSideInfo.getStart(pEdit) == pEditSideInfo.getEnd(pEdit);
  }

  /**
   * Adds a new edit with given old and newIndex into the editList (at the appropriate index)
   *
   * @param pEditList EditList
   * @param pOldIndex startIndex for the original side
   * @param pNewIndex startIndex for the new side
   */
  private static void _fixEditList(EditList pEditList, int pOldIndex, int pNewIndex)
  {
    int index = 0;
    while (pEditList.size() > index && pOldIndex >= pEditList.get(index).getEndA() && pNewIndex >= pEditList.get(index).getEndB())
    {
      index++;
    }
    pEditList.add(index, new Edit(pOldIndex, pOldIndex + 1, pNewIndex, pNewIndex + 1));
  }

  /**
   * Goes throught the editList and combines adjacent or overlapping edits
   *
   * @param pEditList the EditList to compress
   * @return compressed EditList
   */
  private static EditList _compressList(EditList pEditList)
  {
    EditList editList = new EditList();
    int index = 0;
    if (!pEditList.isEmpty())
    {
      Edit edit = pEditList.get(index);
      index++;
      while (index < pEditList.size())
      {
        if (pEditList.get(index - 1).getEndA() < pEditList.get(index).getBeginA() || pEditList.get(index - 1).getEndB() < pEditList.get(index).getBeginB())
        {
          editList.add(new Edit(edit.getBeginA(), pEditList.get(index - 1).getEndA(), edit.getBeginB(), pEditList.get(index - 1).getEndB()));
          edit = pEditList.get(index);
        }
        index++;
      }
      editList.add(new Edit(edit.getBeginA(), pEditList.get(index - 1).getEndA(), edit.getBeginB(), pEditList.get(index - 1).getEndB()));
    }
    return editList;
  }

  /**
   * gets the amount of lines that the other side is offset by for a given line
   *
   * @param pEditList             EditList
   * @param pIndex                index of the line
   * @param pEditSideInformation  IEditSideInfo to extract start and endIndex of the side of the line
   * @param pOtherSideInformation IEditSideInfo to extract start and endIndex of the other side of the line
   * @return number of lines that the other side is offset by for a given line
   */
  private static int _getOffset(EditList pEditList, int pIndex, IEditSideInfo pEditSideInformation, IEditSideInfo pOtherSideInformation)
  {
    int offset = 0;
    for (Edit edit : pEditList)
    {
      if (pEditSideInformation.getStart(edit) < pIndex)
        offset += (pOtherSideInformation.getEnd(edit) - pOtherSideInformation.getStart(edit)) - (pEditSideInformation.getEnd(edit) - pEditSideInformation.getStart(edit));
      else break;
    }
    return offset;
  }

  /**
   * check if the given characters (through the string and index) are different
   *
   * @param pOriginalVersion      String of the original version
   * @param pOriginalVersionIndex index of the character in the original version
   * @param pNewVersion           String of the new version
   * @param pNewVersionIndex      index of the character in the new version
   * @return true if the characters do not match, false otherwise
   */
  private static boolean _isDifferentCharacter(String pOriginalVersion, int pOriginalVersionIndex, String pNewVersion, int pNewVersionIndex)
  {
    return pOriginalVersion.charAt(pOriginalVersionIndex) != pNewVersion.charAt(pNewVersionIndex);
  }

  @Override
  public IChangeDelta acceptChange(EChangeSide pChangedSide)
  {
    // Cannot use the TextEventIndexUpdater here because the indices rely on the other side for their result
    Edit changedEdit;
    ChangeDeltaTextOffsets newChangeDeltaTextOffsets;
    if (pChangedSide == EChangeSide.NEW)
    {
      changedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, startLineIndexNew + (endLineIndexOld - startLineIndexOld));
      newChangeDeltaTextOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew,
                                                             startTextIndexNew + (endTextIndexOld - startTextIndexOld));
    }
    else
    {
      changedEdit = new Edit(startLineIndexOld, startLineIndexOld + (endLineIndexNew - startLineIndexNew), startLineIndexNew, endLineIndexNew);
      newChangeDeltaTextOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, startTextIndexOld + (endTextIndexNew - startTextIndexNew), startTextIndexNew,
                                                             endTextIndexNew);
    }
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(EChangeStatus.ACCEPTED, changeStatus.getChangeType()), newChangeDeltaTextOffsets,
                               textVersionProvider, linePartChangeDeltas);
  }

  @Override
  public IChangeDelta discardChange()
  {
    Edit changedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, endLineIndexNew);
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(EChangeStatus.DISCARDED, changeStatus.getChangeType()),
                               new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew),
                               textVersionProvider, linePartChangeDeltas);
  }

  @Override
  public IChangeDelta setChangeStatus(IChangeStatus pChangeStatus)
  {
    Edit edit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, endLineIndexNew);
    return new ChangeDeltaImpl(edit, pChangeStatus, new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew),
                               textVersionProvider, linePartChangeDeltas);
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    ChangeDeltaImpl that = (ChangeDeltaImpl) pO;
    return startLineIndexNew == that.startLineIndexNew &&
        endLineIndexNew == that.endLineIndexNew &&
        startTextIndexNew == that.startTextIndexNew &&
        endTextIndexNew == that.endTextIndexNew &&
        startLineIndexOld == that.startLineIndexOld &&
        endLineIndexOld == that.endLineIndexOld &&
        startTextIndexOld == that.startTextIndexOld &&
        endTextIndexOld == that.endTextIndexOld &&
        changeStatus.equals(that.changeStatus);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(changeStatus, startLineIndexNew, endLineIndexNew, startTextIndexNew, endTextIndexNew, startLineIndexOld, endLineIndexOld,
                        startTextIndexOld, endTextIndexOld);
  }

  /**
   * Offers functions that change the start and endindices of a given changeSide
   */
  private class TextEventIndexUpdater
  {

    /**
     * Applies the given Functions to the start- and endLineIndices of the specified side and returns the result as edit
     *
     * @param pChangeSide           Which side should have its indices updated
     * @param pStartTextIndexUpdate Function to apply to the startIndex
     * @param pEndTextIndexUpdate   Function to apply to the endIndex
     * @return Edit with updated indices
     */
    private Edit updateLineIndizes(EChangeSide pChangeSide, Function<Integer, Integer> pStartTextIndexUpdate, Function<Integer, Integer> pEndTextIndexUpdate)
    {
      if (pChangeSide == EChangeSide.NEW)
        return new Edit(startLineIndexOld, endLineIndexOld, pStartTextIndexUpdate.apply(startLineIndexNew), pEndTextIndexUpdate.apply(endLineIndexNew));
      return new Edit(pStartTextIndexUpdate.apply(startLineIndexOld), pEndTextIndexUpdate.apply(endLineIndexOld), startLineIndexNew, endLineIndexNew);
    }

    /**
     * Applies the given Functions to the start- and endTextIndices of the specified side and returns the result as edit
     *
     * @param pChangeSide           Which side should have its indices updated
     * @param pStartTextIndexUpdate Function to apply to the startIndex
     * @param pEndTextIndexUpdate   Function to apply to the endIndex
     * @return ChangeDeltaTextOffsets with updated indices
     */
    private ChangeDeltaTextOffsets updateTextIndizes(EChangeSide pChangeSide, Function<Integer, Integer> pStartTextIndexUpdate,
                                                     Function<Integer, Integer> pEndTextIndexUpdate)
    {
      if (pChangeSide == EChangeSide.NEW)
        return new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, pStartTextIndexUpdate.apply(startTextIndexNew), pEndTextIndexUpdate.apply(endTextIndexNew));
      return new ChangeDeltaTextOffsets(pStartTextIndexUpdate.apply(startTextIndexOld), pEndTextIndexUpdate.apply(endTextIndexOld), startTextIndexNew, endTextIndexNew);
    }
  }

  /**
   * Factory that create LinePartChangeDeltaImpls from a given Edit and ChangeDeltaTextOffsets
   */
  private static final class LinePartChangeDeltaFactory implements IChangeDeltaFactory<ILinePartChangeDelta>
  {

    private final int startTextIndexOld;
    private final int startTextIndexNew;

    public LinePartChangeDeltaFactory(int pStartTextIndexOld, int pStartTextIndexNew)
    {
      startTextIndexOld = pStartTextIndexOld;
      startTextIndexNew = pStartTextIndexNew;
    }

    @NotNull
    @Override
    public ILinePartChangeDelta createDelta(@NotNull Edit pEdit, @NotNull ChangeDeltaTextOffsets pDeltaTextOffsets)
    {
      return new LinePartChangeDeltaImpl(EnumMappings.toChangeType(pEdit.getType()), pDeltaTextOffsets.applyOffset(startTextIndexOld, startTextIndexNew));
    }
  }

  /**
   * defines methods to retrieve start and endIndices for a particular side of an edit
   */
  interface IEditSideInfo
  {
    /**
     * get the startIndex of the edit
     *
     * @param pEdit Edit
     * @return startIndex a side specified by the implementation
     */
    int getStart(Edit pEdit);

    /**
     * get the endIndex of the edit
     *
     * @param pEdit Edit
     * @return endIndex a side specified by the implementation
     */
    int getEnd(Edit pEdit);
  }

  /**
   * Implementation for retrieving the original side start and endIndex
   */
  static class OriginalEditSideInfo implements IEditSideInfo
  {

    @Override
    public int getStart(Edit pEdit)
    {
      return pEdit.getBeginA();
    }

    @Override
    public int getEnd(Edit pEdit)
    {
      return pEdit.getEndA();
    }
  }

  /**
   * Implementation for retrieving the new side start and endIndex
   */
  static class NewEditSideInfo implements IEditSideInfo
  {

    @Override
    public int getStart(Edit pEdit)
    {
      return pEdit.getBeginB();
    }

    @Override
    public int getEnd(Edit pEdit)
    {
      return pEdit.getEndB();
    }
  }
}
