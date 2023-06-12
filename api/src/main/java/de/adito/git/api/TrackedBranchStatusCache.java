package de.adito.git.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.TrackedBranchStatus;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache that stores a TrackedBranchStatus for IBranches for a set duration (2 minutes atm)
 * Implementation of the way that a TrachedBranchStatus is derived from an IBranch is up to the extending class
 *
 * @author m.kaspera, 11.03.2020
 */
public abstract class TrackedBranchStatusCache
{

  private final LoadingCache<IBranch, TrackedBranchStatus> statusCache;

  /**
   * Initializes the cache for TrackedBranchStatus
   */
  public TrackedBranchStatusCache()
  {
    statusCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build(CacheLoader.from(this::getTrackedBranchStatus));
  }

  @NonNull
  protected abstract TrackedBranchStatus getTrackedBranchStatus(@NonNull IBranch pBranch);

  /**
   * Retrieves the TrackedBranchStatus for the given branch from the cache, or caluclates it and puts the value in the cache
   *
   * @param pBranch branch for which to determine the TrackedBranchStatus
   * @return TrackedBranchStatus
   */
  @Nullable
  public TrackedBranchStatus get(@NonNull IBranch pBranch)
  {
    try
    {
      return statusCache.get(pBranch);
    }
    catch (ExecutionException pE)
    {
      return null;
    }
  }
}
