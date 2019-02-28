package de.adito.git.impl.data;

import de.adito.git.api.data.IEditorChangeEvent;
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
  private final IEditorChangeEvent editorChange;

  public FileChangesEventImpl(boolean pIsUpdateUI, List<IFileChangeChunk> pCurrentFileChangeChunks, IEditorChangeEvent pEditorChange)
  {
    isUpdateUI = pIsUpdateUI;
    currentFileChangeChunks = pCurrentFileChangeChunks;
    editorChange = pEditorChange;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public IEditorChangeEvent getEditorChange()
  {
    return editorChange;
  }
}
