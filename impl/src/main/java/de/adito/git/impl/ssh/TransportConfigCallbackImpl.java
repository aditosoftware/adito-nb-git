package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.*;
import de.adito.git.api.IUserInputPrompt;
import de.adito.git.api.data.IConfig;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Implementation of {@link org.eclipse.jgit.api.TransportConfigCallback})
 * makes it easier to handle the fetch/clone/push etc per SSH
 *
 * @author m.kaspera 21.09.2018
 */
class TransportConfigCallbackImpl implements TransportConfigCallback
{

  private GitUserInfo gitUserInfo;
  private final IUserInputPrompt userInputPrompt;
  private final IConfig config;
  private String sshKeyPath;

  @Inject
  TransportConfigCallbackImpl(IUserInputPrompt pUserInputPrompt, ISshProvider pSshProvider, @Assisted IConfig pConfig)
  {
    gitUserInfo = pSshProvider.getUserInfo(null, null);
    userInputPrompt = pUserInputPrompt;
    config = pConfig;
  }

  /**
   * @param pTransport {@link org.eclipse.jgit.api.TransportConfigCallback}
   */
  @Override
  public void configure(Transport pTransport)
  {
    SshTransport sshTransport = (SshTransport) pTransport;
    sshTransport.setCredentialsProvider(new ResetCredentialsProvider());
    sshTransport.setSshSessionFactory(new _SshSessionFactory());
  }

  /**
   * for setting the password if the SSH key is password protected
   *
   * @param pPassword the password as String
   */
  public void setPassword(@NotNull String pPassword)
  {
    gitUserInfo.setPassword(pPassword);
  }

  /**
   * for setting the path if the SSH key is not in  the default location (/users/<username>/.ssh for windows)
   *
   * @param pPath path to the location of the ssh key
   */
  public void setSSHKeyLocation(@NotNull String pPath)
  {
    sshKeyPath = pPath;
  }

  /**
   * @param pPassPhrase String with passPhrase for the ssh key
   */
  public void setPassPhrase(@NotNull String pPassPhrase)
  {
    gitUserInfo.setPassPhrase(pPassPhrase);
  }

  /**
   * JschConfigSessionFactory that utilizes password and alternative ssh key locations if need be
   */
  private class _SshSessionFactory extends JschConfigSessionFactory
  {

    @Override
    protected void configure(OpenSshConfig.Host pHost, Session pSession)
    {
      pSession.setUserInfo(gitUserInfo);
    }

    @Override
    protected JSch createDefaultJSch(FS pFs) throws JSchException
    {
      JSch defaultJSch = super.createDefaultJSch(pFs);
      String storedKeyFilePath;
      storedKeyFilePath = config.getSshKeyLocation();
      if (storedKeyFilePath != null && !new File(storedKeyFilePath).exists())
      {
        storedKeyFilePath = null;
      }
      if (storedKeyFilePath != null)
      {
        setSSHKeyLocation(storedKeyFilePath);
      }
      if (sshKeyPath != null)
      {
        /* if there is a default ssh key available it is in this list, and if we don't remove
        it the connection defaults back to the first item in the list (would be the default ssh key)
         */
        defaultJSch.removeAllIdentity();
        defaultJSch.addIdentity(sshKeyPath);
        defaultJSch.setKnownHosts(new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts").getAbsolutePath());
        gitUserInfo.setSshKeyFile(new File(sshKeyPath));
      }
      if (defaultJSch.getIdentityRepository().getIdentities().isEmpty())
      {
        IUserInputPrompt.PromptResult result = userInputPrompt.promptText("Please enter the path to your SSH key");
        if (result.isPressedOK())
        {
          config.setSshKeyLocation(result.getUserInput());
          defaultJSch.addIdentity(result.getUserInput());
          gitUserInfo.setSshKeyFile(new File(result.getUserInput()));
        }
      }
      return defaultJSch;
    }

  }

  /**
   * CredentialsProvider that implements the resetMethod by deleting the set passphrase
   */
  private class ResetCredentialsProvider extends CredentialsProvider
  {

    @Override
    public boolean isInteractive()
    {
      return false;
    }

    @Override
    public void reset(URIish pUri)
    {
      config.setPassphrase(null);
    }

    @Override
    public boolean supports(CredentialItem... pItems)
    {
      return false;
    }

    @Override
    public boolean get(URIish pUri, CredentialItem... pItems)
    {
      return false;
    }
  }

}
