package de.adito.git.impl.data;

import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.data.diff.IFileChangeType;
import lombok.NonNull;

import java.util.List;

/**
 * @author m.kaspera, 14.05.2019
 */
public class DiffInfoImpl implements IDiffInfo
{

  private final ICommit baseCommit;
  private final ICommit parentCommit;
  private final List<IFileChangeType> changedFiles;

  public DiffInfoImpl(@NonNull ICommit pBaseCommit, @NonNull ICommit pParentCommit, @NonNull List<IFileChangeType> pChangedFiles)
  {
    baseCommit = pBaseCommit;
    parentCommit = pParentCommit;
    changedFiles = pChangedFiles;
  }

  @NonNull
  @Override
  public ICommit getBaseCommit()
  {
    return baseCommit;
  }

  @NonNull
  @Override
  public ICommit getParentCommit()
  {
    return parentCommit;
  }

  @NonNull
  @Override
  public List<IFileChangeType> getChangedFiles()
  {
    return changedFiles;
  }
}
