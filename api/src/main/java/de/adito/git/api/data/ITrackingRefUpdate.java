package de.adito.git.api.data;

/**
 * defines the basic information that a class representing a tracking ref update should have
 *
 * @author m.kaspera, 12.11.2019
 */
public interface ITrackingRefUpdate
{

  /**
   * Enum that contains the different result types that can happen on a ref update
   * Types are taken from the TrackingRefUpdate from JGit version 5.6
   */
  enum ResultType
  {
    /**
     * The ref update/delete was not attempted
     */
    NOT_ATTEMPTED(true),

    /**
     * The ref could not be locked for update/delete.
     * <p>
     * This is generally a transient failure and is usually caused by
     * another process trying to access the ref at the same time as this
     * process was trying to update it. It is possible a future operation
     * will be successful.
     *
     * Another possible reason is a left-over lock file, or a branch/ref existing twice
     * (the second attempt to lock it will fail since a lock with that name already exists)
     */
    LOCK_FAILURE(false),

    /**
     * Same value already stored.
     * <p>
     * Both the old value and the new value are identical. No change was
     * necessary for an update. For delete the branch is removed.
     */
    NO_CHANGE(true),

    /**
     * The ref was created locally for an update, but ignored for delete.
     * <p>
     * The ref did not exist when the update started, but it was created
     * successfully with the new value.
     */
    NEW(true),

    /**
     * The ref had to be forcefully updated/deleted.
     * <p>
     * The ref already existed but its old value was not fully merged into
     * the new value. The configuration permitted a forced update to take
     * place, so ref now contains the new value. History associated with the
     * objects not merged may no longer be reachable.
     */
    FORCED(true),

    /**
     * The ref was updated/deleted in a fast-forward way.
     * <p>
     * The tracking ref already existed and its old value was fully merged
     * into the new value. No history was made unreachable.
     */
    FAST_FORWARD(true),

    /**
     * Not a fast-forward and not stored.
     * <p>
     * The tracking ref already existed but its old value was not fully
     * merged into the new value. The configuration did not allow a forced
     * update/delete to take place, so ref still contains the old value. No
     * previous history was lost.
     * <p>
     * <em>Note:</em> Despite the general name, this result only refers to the
     * non-fast-forward case. For more general errors, see {@link
     * #REJECTED_OTHER_REASON}.
     */
    REJECTED(false),

    /**
     * Rejected because trying to delete the current branch.
     * <p>
     * Has no meaning for update.
     */
    REJECTED_CURRENT_BRANCH(false),

    /**
     * The ref was probably not updated/deleted because of I/O error.
     * <p>
     * Unexpected I/O error occurred when writing new ref. Such error may
     * result in uncertain state, but most probably ref was not updated.
     * <p>
     * This kind of error doesn't include LOCK_FAILURE, which is a
     * different case.
     */
    IO_FAILURE(false),

    /**
     * The ref was renamed from another name
     * <p>
     */
    RENAMED(true),

    /**
     * One or more objects aren't in the repository.
     * <p>
     * This is severe indication of either repository corruption on the
     * server side, or a bug in the client wherein the client did not supply
     * all required objects during the pack transfer.
     */
    REJECTED_MISSING_OBJECT(false),

    /**
     * Rejected for some other reason not covered by another enum value.
     */
    REJECTED_OTHER_REASON(false);

    private final boolean isSuccessfull;

    ResultType(boolean pIsSuccessfull)
    {
      isSuccessfull = pIsSuccessfull;
    }

    public boolean isSuccessfull()
    {
      return isSuccessfull;
    }
  }

  /**
   * @return remote name of the updated ref
   */
  String getRemoteName();

  /**
   * @return local name of the updated ref
   */
  String getLocalName();

  /**
   * @return id that the ref pointed to before the update
   */
  String getOldId();

  /**
   * @return id that the ref should point to after the update
   */
  String getNewId();

  /**
   * @return outcome of the update
   */
  ResultType getResult();

}
