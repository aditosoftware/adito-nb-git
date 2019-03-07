package de.adito.git.impl.data;

import de.adito.git.api.data.IEditorChangeEvent;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author m.kaspera, 19.12.2018
 */
public class FileChangesEventImpl implements IFileChangesEvent
{

  private final boolean isUpdateUI;
  private final List<IFileChangeChunk> currentFileChangeChunks;
  private final IEditorChangeEvent editorChange;

  public FileChangesEventImpl(boolean pIsUpdateUI, @NotNull List<IFileChangeChunk> pCurrentFileChangeChunks,
                              @Nullable IEditorChangeEvent pEditorChange)
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
  @NotNull
  @Override
  public List<IFileChangeChunk> getNewValue()
  {
    return currentFileChangeChunks;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public IEditorChangeEvent getEditorChange()
  {
    return editorChange;
  }
}
