package de.adito.git.api;

/**
 * @author m.kaspera 23.11.2018
 */
public interface IUserPreferences {

    /**
     * @return the number of additional Commit History entries that should initially be loaded/loaded when pressing the button at the bottom
     */
    int getNumLoadAdditionalCHEntries();
}
