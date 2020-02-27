package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeStatus;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IChangeStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author m.kaspera, 24.02.2020
 */
public class ChangeStatusImpl implements IChangeStatus
{

  private final EChangeStatus changeStatus;
  private final EChangeType changeType;

  public ChangeStatusImpl(@NotNull EChangeStatus pChangeStatus, @NotNull EChangeType pChangeType)
  {
    changeStatus = pChangeStatus;
    changeType = pChangeType;
  }

  @NotNull
  public EChangeType getChangeType()
  {
    return changeType;
  }

  @NotNull
  public EChangeStatus getChangeStatus()
  {
    return changeStatus;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    ChangeStatusImpl that = (ChangeStatusImpl) pO;
    return changeStatus == that.changeStatus &&
        changeType == that.changeType;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(changeStatus, changeType);
  }
}
