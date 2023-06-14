package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import lombok.NonNull;

/**
 * @author m.kaspera, 19.03.2020
 */
public final class DiffDetailsImpl implements IDiffDetails
{

  private final String oldId;
  private final String newId;
  private final EChangeType changeType;
  private final EFileType oldFileType;
  private final EFileType newFileType;

  public DiffDetailsImpl(@NonNull String pOldId, @NonNull String pNewId, @NonNull EChangeType pChangeType, @NonNull EFileType pOldFileType,
                         @NonNull EFileType pNewFileType)
  {
    oldId = pOldId;
    newId = pNewId;
    changeType = pChangeType;
    oldFileType = pOldFileType;
    newFileType = pNewFileType;
  }

  @NonNull
  @Override
  public String getId(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newId : oldId;
  }

  @NonNull
  @Override
  public EChangeType getChangeType()
  {
    return changeType;
  }

  @NonNull
  @Override
  public EFileType getFileType(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFileType : oldFileType;
  }
}
