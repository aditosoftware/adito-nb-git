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
 * Implementation of the ITransportConfigCallback (and extension of {@link org.eclipse.jgit.api.TransportConfigCallback})
 * makes it easier to handle the fetch/clone/push etc per SSH
 *
 * @author m.kaspera 21.09.2018
 */
public class TransportConfigCallbackImpl implements ITransportConfigCallback  {

    private String password;
    private String sshKeyPath;

    public TransportConfigCallbackImpl(@Nullable String pPassword, @Nullable String pSshKeyPath){
        password = pPassword;
        sshKeyPath = pSshKeyPath;
    }

    public void configure(Transport transport) {
        SshTransport sshTransport = ( SshTransport )transport;
        sshTransport.setSshSessionFactory( new _SshSessionFactory() );
    }

    public void setPassword(@Nullable String pPassword) {
        password = pPassword;
    }

    public void setSSHKeyLocation(@Nullable String pPath){
        sshKeyPath = pPath;
    }

    /**
     * JschConfigSessionFactory that utilizes password and alternative ssh key locations if need be
     */
    private class _SshSessionFactory extends JschConfigSessionFactory {

        protected void configure(OpenSshConfig.Host hc, Session session) {
            if(password != null){
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
        protected JSch createDefaultJSch(FS fs ) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch( fs );
            if(sshKeyPath != null) {
                defaultJSch.addIdentity(sshKeyPath);
            }
            return defaultJSch;
        }
    }

}
