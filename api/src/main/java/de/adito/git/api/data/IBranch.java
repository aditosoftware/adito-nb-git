package de.adito.git.api.data;

/**
 * Definition about what methods a Branch object should provide
 *
 * @author m.kaspera 25.09.2018
 */
public interface IBranch
{

  IBranch ALL_BRANCHES = new SpecialBrancheRepresentative("All");
  IBranch HEAD = new SpecialBrancheRepresentative("HEAD");

  /**
   * @return the name of the Branch in clear text as String, i.e. refs/heads/master
   */
  String getName();

  /**
   * @return the identifier of the Branch as String
   */
  String getId();

  /**
   * @return a String of the short Name, i.e. master. If the branch is a remote branch, the name of the remote is preprended, e.g. origin/master
   */
  String getSimpleName();

  /**
   * @return The actual name of the branch, this is the same as the simpleName except in the case of a remote, where this will return only the name of the branch without
   * the name of the remote prepended
   */
  String getActualName();

  /**
   * Checkout which type of branch the branch is. There are two types of branches to check: heads(remote) and local
   *
   * @return the type of the branch (REMOTE or LOCAL)
   */
  EBranchType getType();

  /**
   * Determines how many commits the branch is behind or ahead of the remote branch. Only useful for local branches that have a remote branch tracked
   *
   * @return TrackedBranchStatus of this branch. TrackedBranchStatus.NONE if the branch is not a local branch or no tracked branch could be found
   */
  TrackedBranchStatus getTrackedBranchStatus();

  /**
   * Signals that no specific branch is chosen
   */
  class SpecialBrancheRepresentative implements IBranch
  {

    private final String name;

    public SpecialBrancheRepresentative(String pName)
    {
      name = pName;
    }

    @Override
    public String getName()
    {
      return name;
    }

    @Override
    public String getId()
    {
      return null;
    }

    @Override
    public String getSimpleName()
    {
      return getName();
    }

    @Override
    public String getActualName()
    {
      return getName();
    }

    @Override
    public EBranchType getType()
    {
      return EBranchType.EMPTY;
    }

    @Override
    public TrackedBranchStatus getTrackedBranchStatus()
    {
      return TrackedBranchStatus.NONE;
    }

    @Override
    public String toString()
    {
      return name;
    }
  }
}
