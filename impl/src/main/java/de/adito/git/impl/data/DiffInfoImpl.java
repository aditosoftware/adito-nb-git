package de.adito.git.impl.data;

import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.data.diff.IFileChangeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author m.kaspera, 14.05.2019
 */
public class DiffInfoImpl implements IDiffInfo
{

  private final ICommit baseCommit;
  private final ICommit parentCommit;
  private final List<IFileChangeType> changedFiles;

  public DiffInfoImpl(@NotNull ICommit pBaseCommit, @NotNull ICommit pParentCommit, @NotNull List<IFileChangeType> pChangedFiles)
  {
    baseCommit = pBaseCommit;
    parentCommit = pParentCommit;
    changedFiles = pChangedFiles;
  }

  @NotNull
  @Override
  public ICommit getBaseCommit()
  {
    return baseCommit;
  }

  @NotNull
  @Override
  public ICommit getParentCommit()
  {
    return parentCommit;
  }

  @NotNull
  @Override
  public List<IFileChangeType> getChangedFiles()
  {
    return changedFiles;
  }
}
