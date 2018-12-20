package de.adito.git.impl.data;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;

import java.util.List;

/**
 * @author m.kaspera, 19.12.2018
 */
public class FileChangesEventImpl implements IFileChangesEvent
{

  private final boolean isUpdateUI;
  private final List<IFileChangeChunk> currentFileChangeChunks;

  public FileChangesEventImpl(boolean pIsUpdateUI, List<IFileChangeChunk> pCurrentFileChangeChunks)
  {
    isUpdateUI = pIsUpdateUI;
    currentFileChangeChunks = pCurrentFileChangeChunks;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isUpdateUI()
  {
    return isUpdateUI;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IFileChangeChunk> getNewValue()
  {
    return currentFileChangeChunks;
  }
}
