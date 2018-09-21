package de.adito.git;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.Transport;

/**
 * Interface for the TransportConfigCallback needed for fetching/cloning/pushing via SSH
 *
 * @author m.kaspera 21.09.2018
 */
public interface ITransportConfigCallback extends TransportConfigCallback {

    /**
     *
     * @param transport {@link org.eclipse.jgit.api.TransportConfigCallback}
     */
    void configure(Transport transport);

    /**
     * for setting the password if the SSH key is password protected
     *
     * @param pword the password as String, {@code null} if no password is required
     */
    void setPassword(String pword);

    /**
     * for setting the path if the SSH key is not in  the default location (/users/<username>/.ssh for windows)
     *
     * @param pPath path to the location of the ssh key, {@code null} if the SSH key is at the default location
     */
    void setSSHKeyLocation(String pPath);

}
