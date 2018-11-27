package de.adito.git.api.data;

/**
 * @author m.kaspera 25.09.2018
 */
public interface IBranch {

    /**
     *
     * @return the name of the Branch in clear text, as String
     */
    String getName();

    /**
     *
     * @return the identifier of the Branch as String
     */
    String getId();

    /**
     *
     * @return a String of the short Name
     */
    String getSimpleName();

    /**
     * Checkout which type of branch the branch is. There are two types of branches to check: heads(remote) and local
     * @return the type of the branch (REMOTE or LOCAL)
     */
    EBranchType getType();

}
