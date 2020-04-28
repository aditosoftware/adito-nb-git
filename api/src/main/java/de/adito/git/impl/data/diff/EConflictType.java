package de.adito.git.impl.data.diff;

/**
 * Denotes the type of conflict two deltas can have. May also indicate they have no conflict
 *
 * @author m.kaspera, 28.04.2020
 */
public enum EConflictType
{
  /**
   * the deltas are different and clash with each other
   */
  CONFLICTING,
  /**
   * Deltas affect the same lines, but are the same for both sides (in terms of text and indices) -> not really a conflict
   */
  SAME,
  /**
   * the deltas itself are different, but, if the diff is based on words instead of lines, can be resolved
   */
  RESOLVABLE,
  /**
   * deltas affect different lines/indices and do not clash with each other
   */
  NONE
}
