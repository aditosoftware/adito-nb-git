package de.adito.git.api.data;

/**
 * Stores how many commits a certain commit is ahead and behind of their tracked branch
 *
 * @author m.kaspera, 11.03.2020
 */
public class TrackedBranchStatus
{

  public static final TrackedBranchStatus NONE = new TrackedBranchStatus(0, 0);
  private final int remoteAheadCount;
  private final int localAheadCount;

  /**
   * @param pRemoteAheadCount number of commits the remote branch is ahead of the local branch
   * @param pLocalAheadCount  number of commits the remote branch is behind the local branch
   */
  public TrackedBranchStatus(int pRemoteAheadCount, int pLocalAheadCount)
  {
    remoteAheadCount = pRemoteAheadCount;
    localAheadCount = pLocalAheadCount;
  }

  /**
   * @return number of commits the remote branch is ahead of the local branch
   */
  public int getRemoteAheadCount()
  {
    return remoteAheadCount;
  }

  /**
   * @return number of commits the remote branch is behind the local branch
   */
  public int getLocalAheadCount()
  {
    return localAheadCount;
  }
}
