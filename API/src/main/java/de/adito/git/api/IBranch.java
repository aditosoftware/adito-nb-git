package de.adito.git.api;

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
}
