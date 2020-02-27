package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @author m.kaspera, 20.02.2020
 */
public class ChangeDeltaImpl implements IChangeDelta
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

  public ChangeDeltaImpl(@NotNull Edit pEdit, @NotNull IChangeStatus pChangeStatus, int pStartTextIndexOld, int pEndTextIndexOld, int pStartTextIndexNew,
                         int pEndTextIndexNew)
  {
    changeStatus = pChangeStatus;
    startLineIndexOld = pEdit.getBeginA();
    endLineIndexOld = pEdit.getEndA();
    startLineIndexNew = pEdit.getBeginB();
    endLineIndexNew = pEdit.getEndB();
    startTextIndexOld = pStartTextIndexOld;
    endTextIndexOld = pEndTextIndexOld;
    startTextIndexNew = pStartTextIndexNew;
    endTextIndexNew = pEndTextIndexNew;
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
                               startTextIndexOld, endTextIndexOld, startTextIndexNew + pTextOffset, endTextIndexNew + pTextOffset);
  }

  @Override
  public List<IChangeDelta> getWordChanges()
  {
    return null;
  }

  @Override
  public IChangeDelta acceptChange()
  {
    Edit changedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, startLineIndexNew + (endLineIndexOld - startLineIndexOld));
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(EChangeStatus.ACCEPTED, changeStatus.getChangeType()),
                               startTextIndexOld, endTextIndexOld, startTextIndexNew, startTextIndexNew + (endTextIndexOld - startTextIndexOld));
  }

  @Override
  public IChangeDelta discardChange()
  {
    Edit changedEdit = new Edit(startLineIndexOld, endLineIndexOld, startLineIndexNew, endLineIndexNew);
    return new ChangeDeltaImpl(changedEdit, new ChangeStatusImpl(EChangeStatus.DISCARDED, changeStatus.getChangeType()),
                               startTextIndexOld, endTextIndexOld, startTextIndexNew, endTextIndexNew);
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
}
