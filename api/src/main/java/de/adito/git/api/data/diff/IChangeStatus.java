package de.adito.git.api.data.diff;

/**
 * Combination of ChangeType and ChangeStatus, giving information about the kind of change and if the change was touched by the used yet
 *
 * @author m.kaspera, 24.02.2020
 */
public interface IChangeStatus
{

  EChangeType getChangeType();

  EChangeStatus getChangeStatus();

}
