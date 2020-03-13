package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
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
  public IChangeDelta applyOffset(int pLineOffset, int pTextOffset)
  {
    Edit changedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew + pLineOffset, endLineIndexOld + pLineOffset);
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(getChangeStatus().getChangeStatus(), getChangeStatus().getChangeType()),
                               new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew + pTextOffset, endTextIndexNew + pTextOffset),
                               textVersionProvider,
                               linePartChangeDeltas == null ? null : linePartChangeDeltas.stream()
                                   .map(pILinePartChangeDelta -> pILinePartChangeDelta.applyOffset(pTextOffset))
                                   .collect(Collectors.toList()));
  }

  @Override
  public IChangeDelta processTextEvent(int pOffset, int pLength, int pNumNewlinesBefore, int pNumNewlines, boolean pIsInsert)
  {
    Edit modifiedEdit;
    IChangeStatus modifiedChangeStatus = new ChangeStatusImpl(EChangeStatus.UNDEFINED, changeStatus.getChangeType());
    ChangeDeltaTextOffsets modifiedChangeDeltaOffsets;
    if (pIsInsert)
    {
      modifiedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, endLineIndexNew + pNumNewlines);
      modifiedChangeDeltaOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew + pLength);
    }
    else
    {
      if (pOffset < startTextIndexNew)
      {
        if (pOffset + -pLength < endTextIndexNew)
        {
          // case DELETE 1
          modifiedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew + pNumNewlinesBefore, endLineIndexNew + pNumNewlines + pNumNewlinesBefore);
          modifiedChangeDeltaOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew + (pOffset - startTextIndexNew),
                                                                  endTextIndexNew + pLength);
        }
        else
        {
          // case DELETE 2
          modifiedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew + pNumNewlinesBefore, endLineIndexNew + pNumNewlines + pNumNewlinesBefore);
          modifiedChangeDeltaOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, pOffset, pOffset);
        }
      }
      else
      {
        if (pOffset + -pLength <= endTextIndexNew)
        {
          // case DELETE 3/7
          modifiedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, endLineIndexNew + pNumNewlines);
          modifiedChangeDeltaOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew + pLength);
        }
        else
        {
          // case DELETE 4
          modifiedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, endLineIndexNew + pNumNewlines);
          modifiedChangeDeltaOffsets = new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew - (endTextIndexNew - pOffset));
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
              .stream().anyMatch(pOwnLinePartChangeDelta -> pOwnLinePartChangeDelta.isConflictingWith(pLinePartChangeDelta) && _isSameChange(pOtherChangeDelta)));
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

  /**
   * @param pOtherChangeDelta the IChangeDelta to compare this delta to
   * @return true if the change is the same, false otherwise
   */
  private boolean _isSameChange(IChangeDelta pOtherChangeDelta)
  {
    return !(startTextIndexOld == pOtherChangeDelta.getStartTextIndex(EChangeSide.OLD) && endTextIndexOld == pOtherChangeDelta.getEndTextIndex(EChangeSide.OLD)
        && getText(EChangeSide.NEW).equals(pOtherChangeDelta.getText(EChangeSide.NEW)));
  }


  private List<ILinePartChangeDelta> _calculateLinePartChangeDeltas()
  {
    String originalVersion;
    // if the type is add, make sure the original version contains only empty text
    if (changeStatus.getChangeType() == EChangeType.ADD)
      originalVersion = "";
    else
      // the diff only works on a line-basis, so we split our line such that each word is on a separate line
      originalVersion = textVersionProvider.getVersion(EChangeSide.OLD).substring(startTextIndexOld, endTextIndexOld).replace(" ", "\n");
    String newVersion = textVersionProvider.getVersion(EChangeSide.NEW).substring(startTextIndexNew, endTextIndexNew).replace(" ", "\n");
    EditList editList = LineIndexDiffUtil.getChangedLines(originalVersion, newVersion, RawTextComparator.DEFAULT);
    return LineIndexDiffUtil.getTextOffsets(originalVersion, newVersion, editList,
                                            new LinePartChangeDeltaFactory(startTextIndexOld, startTextIndexNew));
  }

  @Override
  public IChangeDelta acceptChange()
  {
    Edit changedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, startLineIndexNew + (endLineIndexOld - startLineIndexOld));
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(EChangeStatus.ACCEPTED, changeStatus.getChangeType()),
                               new ChangeDeltaTextOffsets(startTextIndexOld, endTextIndexOld, startTextIndexNew,
                                                          startTextIndexNew + (endTextIndexOld - startTextIndexOld)),
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
}