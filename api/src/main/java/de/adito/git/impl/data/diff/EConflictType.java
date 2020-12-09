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
   * The THEIRS change is a part of the YOURS change -> accepting the YOURS change yields the changes from both sides -> not a conflict
   */
  ENCLOSED_BY_YOURS,
  /**
   * The YOURS change is a part of the THEIRS change -> accepting the THEIRS change yields the changes from both sides -> not a conflict
   */
  ENCLOSED_BY_THEIRS,
  /**
   * deltas affect different lines/indices and do not clash with each other
   */
  NONE
}
