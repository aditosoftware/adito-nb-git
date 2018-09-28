package de.adito.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link org.eclipse.jgit.api.TransportConfigCallback})
 * makes it easier to handle the fetch/clone/push etc per SSH
 *
 * @author m.kaspera 21.09.2018
 */
class TransportConfigCallbackImpl implements TransportConfigCallback {

    private String password;
    private String sshKeyPath;

    TransportConfigCallbackImpl(@Nullable String pPassword, @Nullable String pSshKeyPath) {
        password = pPassword;
        sshKeyPath = pSshKeyPath;
    }

    /**
     * @param transport {@link org.eclipse.jgit.api.TransportConfigCallback}
     */
    public void configure(Transport transport) {
        SshTransport sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(new _SshSessionFactory());
    }

    /**
     * for setting the password if the SSH key is password protected
     *
     * @param pPassword the password as String, {@code null} if no password is required
     */
    public void setPassword(@Nullable String pPassword) {
        password = pPassword;
    }

    /**
     * for setting the path if the SSH key is not in  the default location (/users/<username>/.ssh for windows)
     *
     * @param pPath path to the location of the ssh key, {@code null} if the SSH key is at the default location
     */
    public void setSSHKeyLocation(@Nullable String pPath) {
        sshKeyPath = pPath;
    }

    /**
     * JschConfigSessionFactory that utilizes password and alternative ssh key locations if need be
     */
    private class _SshSessionFactory extends JschConfigSessionFactory {

        protected void configure(OpenSshConfig.Host hc, Session session) {
            if (password != null) {
                session.setUserInfo(new UserInfo() {
                    public String getPassphrase() {
                        return password;
                    }

                    public String getPassword() {
                        return null;
                    }

                    public boolean promptPassword(String message) {
                        return false;
                    }

                    public boolean promptPassphrase(String message) {
                        return true;
                    }

                    public boolean promptYesNo(String message) {
                        return false;
                    }

                    public void showMessage(String message) {

                    }
                });
            }
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            if (sshKeyPath != null) {
                /* if there is a default ssh key available it is in this list, and if we don't remove
                it the connection defaults back to the first item in the list (would be the default ssh key)
                 */
                defaultJSch.removeAllIdentity();
                defaultJSch.addIdentity(sshKeyPath);
            }
            return defaultJSch;
        }
    }

}
