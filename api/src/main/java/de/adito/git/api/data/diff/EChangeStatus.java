package de.adito.git.api.data.diff;

/**
 * Represents if the user has accepted a change, discarded it or not done anything about it yet
 *
 * @author m.kaspera, 24.02.2020
 */
public enum EChangeStatus
{
  /**
   * Change was not accepted or discarded yet
   */
  PENDING,
  /**
   * Change has been accepted
   */
  ACCEPTED,
  /**
   * Change has been discarded
   */
  DISCARDED,
  /**
   * Status cannot be defined, e.g. for ChangeDeltas that represent no change
   */
  UNDEFINED
}
