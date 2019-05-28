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

    private String allBranchName = "All";

    @Override
    public String getName()
    {
      return allBranchName;
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
    public EBranchType getType()
    {
      return EBranchType.EMPTY;
    }

    @Override
    public String toString()
    {
      return allBranchName;
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
   * @return a String of the short Name, i.e. master
   */
  String getSimpleName();

  /**
   * Checkout which type of branch the branch is. There are two types of branches to check: heads(remote) and local
   *
   * @return the type of the branch (REMOTE or LOCAL)
   */
  EBranchType getType();

}
