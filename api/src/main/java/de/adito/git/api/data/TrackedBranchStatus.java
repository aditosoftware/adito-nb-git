package de.adito.git.api.data;

import org.jetbrains.annotations.Nullable;

/**
 * Stores how many commits a certain commit is ahead and behind of their tracked branch
 *
 * @author m.kaspera, 11.03.2020
 */
public class TrackedBranchStatus
{

  public static final TrackedBranchStatus NONE = new TrackedBranchStatus(null, 0, 0);
  private final String remoteTrackedBranchName;
  private final int remoteAheadCount;
  private final int localAheadCount;

  /**
   * @param pRemoteTrackedBranchName Name of the remote tracked branch
   * @param pRemoteAheadCount        number of commits the remote branch is ahead of the local branch
   * @param pLocalAheadCount         number of commits the remote branch is behind the local branch
   */
  public TrackedBranchStatus(@Nullable String pRemoteTrackedBranchName, int pRemoteAheadCount, int pLocalAheadCount)
  {
    remoteTrackedBranchName = pRemoteTrackedBranchName;
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

  /**
   * @return Name of the remote tracked branch
   */
  @Nullable
  public String getRemoteTrackedBranchName()
  {
    return remoteTrackedBranchName;
  }
}
