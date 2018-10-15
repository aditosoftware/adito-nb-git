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
     * @param branch the branch for the shot name
     * @return a String of the short Name
     */
    String getSimpleName(IBranch branch);


}
