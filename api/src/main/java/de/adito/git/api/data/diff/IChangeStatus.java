package de.adito.git.api.data.diff;

import de.adito.git.impl.data.diff.EConflictType;

import java.awt.Color;

/**
 * Combination of ChangeType and ChangeStatus, giving information about the kind of change and if the change was touched by the used yet
 *
 * @author m.kaspera, 24.02.2020
 */
public interface IChangeStatus
{

  EChangeType getChangeType();

  EConflictType getConflictType();

  EChangeStatus getChangeStatus();

  /**
   * @return Color to use when marking an area of this changeStatus
   */
  default Color getDiffColor()
  {
    if (getChangeStatus() == EChangeStatus.PENDING)
    {
      if (getConflictType() == EConflictType.CONFLICTING)
      {
        return EChangeType.CONFLICTING.getDiffColor();
      }
      else
      {
        return getChangeType().getDiffColor();
      }
    }
    else
    {
      return getSecondaryDiffColor();
    }
  }

  /**
   * @return Color to use when marking an area of this changeStatus and the marking should be in the background
   */
  default Color getSecondaryDiffColor()
  {
    if (getConflictType() == EConflictType.CONFLICTING)
    {
      return EChangeType.CONFLICTING.getSecondaryDiffColor();
    }
    else
    {
      return getChangeType().getSecondaryDiffColor();
    }
  }

}
