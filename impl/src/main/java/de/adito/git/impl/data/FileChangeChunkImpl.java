package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author m.kaspera 05.10.2018
 */
public class FileChangeChunkImpl implements IFileChangeChunk
{

  private final Edit edit;
  private final String oldString;
  private final String newString;
  private final EChangeType changeType;

  public FileChangeChunkImpl(IFileChangeChunk pChangeChunk, EChangeType pChangeType)
  {
    this(new Edit(pChangeChunk.getStart(EChangeSide.OLD), pChangeChunk.getEnd(EChangeSide.OLD), pChangeChunk.getStart(EChangeSide.NEW),
                  pChangeChunk.getEnd(EChangeSide.NEW)), pChangeChunk.getLines(EChangeSide.OLD), pChangeChunk.getLines(EChangeSide.NEW), pChangeType);
  }

  FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString)
  {
    this(pEdit, pOldString, pNewString, null);

  }

  public FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString, @Nullable EChangeType pChangeType)
  {
    edit = pEdit;
    oldString = pOldString;
    newString = pNewString;
    changeType = pChangeType;
  }

  @Override
  public int getStart(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.OLD ? edit.getBeginA() : edit.getBeginB();
  }

  @Override
  public int getEnd(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.OLD ? edit.getEndA() : edit.getEndB();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EChangeType getChangeType()
  {
    if (changeType != null)
    {
      return changeType;
    }
    EChangeType returnValue;
    switch (edit.getType())
    {
      case REPLACE:
        returnValue = EChangeType.MODIFY;
        break;
      case INSERT:
        returnValue = EChangeType.ADD;
        break;
      case DELETE:
        returnValue = EChangeType.DELETE;
        break;
      default:
        returnValue = null;
    }
    return returnValue;
  }

  @Override
  public String getLines(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.OLD ? oldString : newString;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    FileChangeChunkImpl that = (FileChangeChunkImpl) pO;
    return Objects.equals(edit, that.edit) &&
        Objects.equals(oldString, that.oldString) &&
        Objects.equals(newString, that.newString) &&
        changeType == that.changeType;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(edit, oldString, newString, changeType);
  }
}
