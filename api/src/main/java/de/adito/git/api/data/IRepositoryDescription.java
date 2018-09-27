package de.adito.git.api.data;

/**
 * Interface for the object that stores the information that is necessary to construct a repository
 * plus some additional information about the user using the repository
 *
 * @author m.kaspera 27.09.2018
 */
public interface IRepositoryDescription {

    String getPath();

    String getEmail();

    String getUsername();

    String getPassword();

    String getSSHKeyLocation();

    String getPassphrase();
}
