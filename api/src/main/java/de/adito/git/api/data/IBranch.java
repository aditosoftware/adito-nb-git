package de.adito.git.api.data;

/**
 * Definition about what methods a Branch object should provide
 *
 * @author m.kaspera 25.09.2018
 */
public interface IBranch
{

  /**
   * Signals that no specific branch is chosen
   */
  IBranch ALL_BRANCHES = new IBranch()
  {

    private static final String ALL_BRANCH_NAME = "All";

    @Override
    public String getName()
    {
      return ALL_BRANCH_NAME;
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
    public String toString()
    {
      return ALL_BRANCH_NAME;
    }
  };

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

}
