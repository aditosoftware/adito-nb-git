package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.adito.git.api.*;
import de.adito.git.api.data.IConfig;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.impl.http.GitHttpUtil;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private final IAuthUtil authUtil;
  private final IPrefStore prefStore;
  private final IKeyStore keyStore;
  private final GitUserInfo gitUserInfo;
  private final IUserInputPrompt userInputPrompt;
  private final IConfig config;
  private String sshKeyPath;

  @Inject
  TransportConfigCallbackImpl(IAuthUtil pAuthUtil, IPrefStore pPrefStore, IKeyStore pKeyStore, IUserInputPrompt pUserInputPrompt, ISshProvider pSshProvider, @Assisted IConfig pConfig)
  {
    authUtil = pAuthUtil;
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
      String realmName = "GitLab";
      URIish transportURI = pTransport.getURI();
      try
      {
        realmName = GitHttpUtil.getRealmName(transportURI.toString(), prefStore, keyStore);
      }
      catch (Exception pE)
      {
        logger.log(Level.WARNING, pE, () -> "Error while determining the realm name for URI" + transportURI.toString());
      }

      httpTransport.setCredentialsProvider(new AditoCredentialsProvider(authUtil,
                                                                        transportURI.getHost(), null, transportURI.getPort(),
                                                                        transportURI.getScheme(), realmName, "Basic"));
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
   * Try and get the ssh key that will be used for auth purposes, either by getting it from the gitUserInfo or by checking the config for the set location for the
   * given remote url
   *
   * @param pRemoteUrl remote url to fall back on
   * @return path to the ssh key that will be used for auth purposes
   */
  @Nullable
  private String getSetSSHKeyLocation(@Nullable String pRemoteUrl)
  {
    File sshKeyFile = gitUserInfo.getSshKeyFile();
    if (sshKeyFile != null)
    {
      return sshKeyFile.toString();
    }
    else if (pRemoteUrl != null)
    {
      return config.getSshKeyLocation(pRemoteUrl);
    }
    return null;
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
      /*
       Remove all identities since the plugin has to handle at least part of getting the credentials, and having half done by JSch and half by the plugin
       inevitably leads to problems
       */
      defaultJSch.removeAllIdentity();
      defaultJSch.setKnownHosts(new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts").getAbsolutePath());
      if (sshKeyPath != null)
      {
        defaultJSch.addIdentity(sshKeyPath);
        gitUserInfo.setSshKeyFile(new File(sshKeyPath));
      }
      else
      {
        String promptString;
        String remoteName = config.getRemoteName(remoteUrl);
        if (remoteName != null)
        {
          promptString = "Please enter the path to your SSH key for remote \"" + remoteName + "\":";
        }
        else
        {
          promptString = "Please enter the path to your SSH key for the remote with url \"" + remoteUrl + "\":";
        }
        IUserInputPrompt.PromptResult result = userInputPrompt.promptSSHInfo(promptString, null, null, keyStore);
        if (result.isPressedOK())
        {
          config.setSshKeyLocationForUrl(result.getUserInput(), remoteUrl);
          defaultJSch.addIdentity(result.getUserInput());
          gitUserInfo.setSshKeyFile(new File(result.getUserInput()));
          if (result.getUserArrayInput() != null)
          {
            gitUserInfo.setPassPhrase(String.valueOf(result.getUserArrayInput()));
            config.setPassphrase(result.getUserArrayInput(), getSetSSHKeyLocation(remoteUrl));
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
      String sshKeyLocation = getSetSSHKeyLocation(pUri.toString());
      if (sshKeyLocation != null)
      {
        config.setPassphrase(null, config.getSshKeyLocation(pUri.toString()));
      }
      else
      {
        logger.log(Level.INFO, () -> "Tried to reset passphrase for pUri " + pUri + " but couldn't find matching repository");
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
