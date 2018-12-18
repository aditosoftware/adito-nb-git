package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.Nullable;

/**
 * @author m.kaspera 05.10.2018
 */
public class FileChangeChunkImpl implements IFileChangeChunk
{

  private final Edit edit;
  private final String oldString;
  private final String newString;
  private final String oldParityLines;
  private final String newParityLines;
  private final EChangeType changeType;

  FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString)
  {
    this(pEdit, pOldString, pNewString, "", "", null);
  }

  public FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString, @Nullable EChangeType pChangeType)
  {
    this(pEdit, pOldString, pNewString, "", "", pChangeType);
  }

  public FileChangeChunkImpl(IFileChangeChunk pChangeChunk, EChangeType pChangeType)
  {
    this(new Edit(pChangeChunk.getAStart(), pChangeChunk.getAEnd(), pChangeChunk.getBStart(), pChangeChunk.getBEnd()),
         pChangeChunk.getALines(), pChangeChunk.getBLines(), pChangeChunk.getAParityLines(), pChangeChunk.getBParityLines(), pChangeType);
  }

  FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString, String pOldParityLines, String pNewParityLines)
  {
    this(pEdit, pOldString, pNewString, pOldParityLines, pNewParityLines, null);

  }

  FileChangeChunkImpl(Edit pEdit, String pOldString, String pNewString, String pOldParityLines, String pNewParityLines,
                      @Nullable EChangeType pChangeType)
  {
    edit = pEdit;
    oldString = pOldString;
    newString = pNewString;
    changeType = pChangeType;
    oldParityLines = pOldParityLines;
    newParityLines = pNewParityLines;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAStart()
  {
    return edit.getBeginA();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAEnd()
  {
    return edit.getEndA();
  }

  @Override
  public int getBStart()
  {
    return edit.getBeginB();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getBEnd()
  {
    return edit.getEndB();
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String getALines()
  {
    return oldString;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBLines()
  {
    return newString;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAParityLines()
  {
    return oldParityLines;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBParityLines()
  {
    return newParityLines;
  }
}
