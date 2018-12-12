package de.adito.git.api.data;

public enum EPushResult
{

  /**
   * Remote ref was up to date, there was no need to update anything.
   */
  UP_TO_DATE,

  /**
   * Remote ref update was rejected, as it would cause non fast-forward
   * update.
   */
  REJECTED_NON_FAST_FORWARD,

  /**
   * Remote ref update was rejected, because remote side doesn't
   * support/allow deleting refs.
   */
  REJECTED_NO_DELETE,

  /**
   * Remote ref update was rejected, because old object id on remote
   * repository wasn't the same as defined expected old object.
   */
  REJECTED_REMOTE_CHANGED,

  /**
   * Remote ref update was rejected for some other reason
   */
  REJECTED_OTHER_REASON,

  /**
   * Remote ref didn't exist. Can occur on delete request of a non
   * existing ref.
   */
  NON_EXISTING,

  /**
   * Remote ref was successfully updated.
   */
  OK;

  boolean isSuccess()
  {
    return this == OK || this == UP_TO_DATE;
  }

}
