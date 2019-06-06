package de.adito.git.api.data;

import java.util.List;

/**
 * Interface that defines the class that keeps the result of a Rebase operation
 *
 * @author m.kaspera 05.12.2018
 */
public interface IRebaseResult
{

  /**
   * FINISHED:              Rebase finished successfully
   * ABORTED:               Rebase operation was aborted
   * UP_TO_DATE:            Rebase didn't do anything, files were already up to date
   * STOPPED:               Rebase stopped, probably due to a conflict
   * FAILED:                Rebase operation failed
   * EXISTING_CONFLICTS:    Rebase operation cannot continue because there are still existing conflicts
   * FAST_FORWARD:          Rebase was fast-forwarded
   */
  enum ResultType
  {FINISHED, ABORTED, UP_TO_DATE, STOPPED, FAILED, EXISTING_CONFLICTS, FAST_FORWARD}

  List<IMergeDiff> getMergeConflicts();

  ResultType getResultType();

  boolean isSuccess();

}
