package de.adito.git.api.data;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for the object that stores the information that is necessary to construct a repository
 * plus some additional information about the user using the repository
 *
 * @author m.kaspera 27.09.2018
 */
public interface IRepositoryDescription {

    /**
     *
     * @return String with the path to the repository
     */
    String getPath();

    /**
     *
     * @return String with the email address used by the user for committing/pushing...
     */
    String getEmail();

    /**
     *
     * @return String with the username used for committing/pushing...
     */
    String getUsername();

    /**
     *
     * @return String with password used for authentication purposes. Can be null if ssh key is used
     */
    @Nullable
    String getPassword();

    /**
     *
     * @return String with the location to the ssh key, {@code null} if the default ssh key is
     * used or no ssh key is used at all
     */
    @Nullable
    String getSSHKeyLocation();

    /**
     *
     * @return String with passphrase for the ssh key, if encrypted. {@code null} if the key is not encrypted or no key is used
     */
    @Nullable
    String getPassphrase();
}
