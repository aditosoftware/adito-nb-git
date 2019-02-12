package de.adito.git.api.data;

import java.util.List;

/**
 * Interface that defines how to describe the outcome of a cherryPick operation
 *
 * @author m.kaspera, 11.02.2019
 */
public interface ICherryPickResult
{

  /**
   * Represents the types of results that are possible for a CherryPick operation
   */
  enum ResultType
  {
    /**
     * all commits were successfully cherry picked
     */
    OK,
    /**
     * one of the cherry picks resulted in a conflict
     */
    CONFLICTING,
    /**
     * cherry picked failed for other reasons than a conflict
     */
    FAILED
  }

  /**
   * @return ResultType that describes the outcome of the CherryPick
   */
  ResultType getResult();

  /**
   * @return null if status was OK, the commit that was cherry picked last and caused a conflict otherwise
   */
  ICommit getCherryPickHead();

  /**
   * @return list of IMergeDiffs that describe the conflicts and can be used in the MergeConflictDialog
   */
  List<IMergeDiff> getConflicts();

}
