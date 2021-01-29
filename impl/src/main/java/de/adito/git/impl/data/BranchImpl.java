package de.adito.git.impl.data;

import de.adito.git.api.TrackedBranchStatusCache;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.TrackedBranchStatus;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author m.kaspera 25.09.2018
 */
public class BranchImpl implements IBranch
{

  public static final String REF_STRING = "refs/";
  public static final String HEAD_STRING = "heads/";
  public static final String REMOTE_STRING = "remotes/";

  @Nullable // this only happens if the type is DETACHED
  private final Ref branchRef;
  private final TrackedBranchStatusCache trackedBranchStatusCache;
  private EBranchType branchType;
  private final String simpleName;

  public BranchImpl(@NotNull Ref pBranchRef, @NotNull TrackedBranchStatusCache pTrackedBranchStatusCache)
  {
    branchRef = pBranchRef;
    trackedBranchStatusCache = pTrackedBranchStatusCache;
    branchType = EBranchType.EMPTY;
    String simpleNameRaw = getName();
    if (simpleNameRaw.startsWith(REF_STRING))
    {
      simpleNameRaw = simpleNameRaw.substring(REF_STRING.length());
      if (simpleNameRaw.startsWith(HEAD_STRING))
      {
        simpleNameRaw = simpleNameRaw.substring(HEAD_STRING.length());
        branchType = EBranchType.LOCAL;
      }
      else if (simpleNameRaw.startsWith(REMOTE_STRING))
      {
        simpleNameRaw = simpleNameRaw.substring(REMOTE_STRING.length());
        branchType = EBranchType.REMOTE;
      }
    }
    if (getName().split("/").length == 1 && "DETACHED".equals(branchRef.getName()))
    {
      branchType = EBranchType.DETACHED;
      simpleNameRaw = getId();
    }
    this.simpleName = simpleNameRaw;
  }

  public BranchImpl(@NotNull ObjectId pId, @NotNull TrackedBranchStatusCache pTrackedBranchStatusCache)
  {
    trackedBranchStatusCache = pTrackedBranchStatusCache;
    branchType = EBranchType.DETACHED;
    simpleName = ObjectId.toString(pId);
    branchRef = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName()
  {
    if (branchType == EBranchType.DETACHED || branchRef == null)
    {
      return simpleName;
    }
    return branchRef.getName();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId()
  {
    if (branchType != EBranchType.DETACHED && branchRef != null)
      return ObjectId.toString(branchRef.getObjectId());
    else
      return simpleName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSimpleName()
  {
    return simpleName;
  }

  @Override
  public String getActualName()
  {
    String name = getSimpleName();
    if (branchType == EBranchType.REMOTE)
      name = name.substring(name.indexOf('/') + 1);
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EBranchType getType()
  {
    return branchType;
  }

  @Override
  public TrackedBranchStatus getTrackedBranchStatus()
  {
    return trackedBranchStatusCache.get(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    if (EBranchType.DETACHED == this.getType() || branchRef == null)
      return this.getId();
    return branchRef.getName();
  }

  @Override
  public int hashCode()
  {
    return (155 + getId().hashCode() + getName().hashCode() + getType().hashCode()) * 43;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(IBranch.class.isAssignableFrom(obj.getClass())))
      return false;
    IBranch branch = (IBranch) obj;
    return getId() != null && getId().equals(branch.getId()) && getName().equals(branch.getName()) && branch.getType() == getType();
  }
}
