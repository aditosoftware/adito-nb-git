package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IUserInputPrompt;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.exception.UnknownRemoteRepositoryException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.impl.http.GitHttpUtil;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link org.eclipse.jgit.api.TransportConfigCallback})
 * makes it easier to handle the fetch/clone/push etc per SSH
 *
 * @author m.kaspera 21.09.2018
 */
class TransportConfigCallbackImpl implements TransportConfigCallback
{

  private final Logger logger = Logger.getLogger(TransportConfigCallbackImpl.class.getName());
  private final IPrefStore prefStore;
  private final IKeyStore keyStore;
  private final GitUserInfo gitUserInfo;
  private final IUserInputPrompt userInputPrompt;
  private final IConfig config;
  private String sshKeyPath;

  @Inject
  TransportConfigCallbackImpl(IPrefStore pPrefStore, IKeyStore pKeyStore, IUserInputPrompt pUserInputPrompt, ISshProvider pSshProvider, @Assisted IConfig pConfig)
  {
    prefStore = pPrefStore;
    keyStore = pKeyStore;
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
    //ssh and https transport is supported
    if (pTransport instanceof SshTransport)
    {
      SshTransport sshTransport = (SshTransport) pTransport;
      sshTransport.setCredentialsProvider(new ResetCredentialsProvider());
      sshTransport.setSshSessionFactory(new _SshSessionFactory(sshTransport.getURI().toString()));
    }
    else if (pTransport instanceof HttpTransport)
    {
      HttpTransport httpTransport = (HttpTransport) pTransport;
      String realmName = GitHttpUtil.getRealmName(pTransport.getURI().toString());
      // netbeans stores the username in the preferences of the org/netbeans/core/authentication node, the key is the realm name. The password is stored in the KeyRing
      // with authentication.realmName as key
      // Suppliers are used because when this is called the first time, the username and password is not yet set -> can be re-evaluated once required
      httpTransport.setCredentialsProvider(new AditoUsernamePasswordCredentialsProvider(() -> prefStore.get("org/netbeans/core/authentication", realmName),
                                                                                        () -> keyStore.read("authentication." + realmName)));
    }
    else throw new RuntimeException("Unsupported Transport protocol, make sure the project is configured to use either ssh or http");
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

    private final String remoteUrl;

    _SshSessionFactory(String pRemoteUrl)
    {
      remoteUrl = pRemoteUrl;
    }

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
      storedKeyFilePath = config.getSshKeyLocation(remoteUrl);
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
        IUserInputPrompt.PromptResult result = userInputPrompt.promptSSHInfo("Please enter the path to your SSH key for remote \"" + config.getRemoteName(remoteUrl)
                                                                                 + "\":", null, null, keyStore);
        if (result.isPressedOK())
        {
          config.setSshKeyLocation(result.getUserInput(), remoteUrl);
          defaultJSch.addIdentity(result.getUserInput());
          gitUserInfo.setSshKeyFile(new File(result.getUserInput()));
          if (result.getUserArrayInput() != null)
          {
            gitUserInfo.setPassPhrase(String.valueOf(result.getUserArrayInput()));
          }
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
      try
      {
        config.setPassphrase(null, pUri.toString());
      }
      catch (UnknownRemoteRepositoryException pE)
      {
        logger.log(Level.INFO, pE, () -> "Tried to reset passphrase for pUri " + pUri + " but couldn't find matching repository");
      }
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
