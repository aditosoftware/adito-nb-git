package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.adito.git.api.IUserInputPrompt;
import de.adito.git.impl.RepositoryImplHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

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
  private final Git git;
  private String sshKeyPath;

  @Inject
  TransportConfigCallbackImpl(IUserInputPrompt pUserInputPrompt, ISshProvider pSshProvider, @Assisted Git pGit)
  {
    gitUserInfo = pSshProvider.getUserInfo(null, null);
    userInputPrompt = pUserInputPrompt;
    git = pGit;
  }

  /**
   * @param pTransport {@link org.eclipse.jgit.api.TransportConfigCallback}
   */
  @Override
  public void configure(Transport pTransport)
  {
    SshTransport sshTransport = (SshTransport) pTransport;
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

    private static final String SSH_KEY_CONFIG_KEY = "puttykeyfile";
    private static final String SECTION_KEY = "remote";

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
      try
      {
        storedKeyFilePath = git.getRepository()
            .getConfig().getString(SECTION_KEY, git.getRepository()
                .getRemoteName(RepositoryImplHelper.getRemoteTrackingBranch(git)), SSH_KEY_CONFIG_KEY);
        if (storedKeyFilePath != null && !new File(storedKeyFilePath).exists())
        {
          storedKeyFilePath = null;
        }
      }
      catch (IOException pE)
      {
        throw new RuntimeException(pE);
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
        gitUserInfo.setSshKeyFile(new File(sshKeyPath));
      }
      if (defaultJSch.getIdentityRepository().getIdentities().isEmpty())
      {
        IUserInputPrompt.PromptResult result = userInputPrompt.promptText("Please enter the path to your SSH key");
        if (result.isPressedOK())
        {
          try
          {
            StoredConfig config = git.getRepository().getConfig();
            config.setString(SECTION_KEY, git.getRepository()
                .getRemoteName(RepositoryImplHelper.getRemoteTrackingBranch(git)), SSH_KEY_CONFIG_KEY, result.getUserInput());
            config.save();
          }
          catch (IOException pE)
          {
            throw new RuntimeException(pE);
          }
          defaultJSch.addIdentity(result.getUserInput());
          gitUserInfo.setSshKeyFile(new File(result.getUserInput()));
        }
      }
      return defaultJSch;
    }

  }

}
