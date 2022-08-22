package de.adito.git.impl.ssh;

import de.adito.git.api.IAuthUtil;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.*;

import java.net.*;

/**
 * @author p.neub, 19.08.2022
 */
public class AditoCredentialsProvider extends CredentialsProvider
{

  private final IAuthUtil authUtil;
  private final String host;
  private final InetAddress addr;
  private final int port;
  private final String protocol;
  private final String prompt;
  private final String sheme;
  private PasswordAuthentication auth;

  public AditoCredentialsProvider(IAuthUtil pAuthUtil, String pHost, InetAddress pAddr, int pPort, String pProtocol, String pPrompt, String pSheme)
  {
    authUtil = pAuthUtil;
    host = pHost;
    addr = pAddr;
    port = pPort;
    protocol = pProtocol;
    prompt = pPrompt;
    sheme = pSheme;
    auth = authUtil.getAuth();
  }

  @Override
  public boolean isInteractive()
  {
    return true;
  }

  @Override
  public boolean supports(CredentialItem... items)
  {
    for (CredentialItem item : items)
    {
      boolean isUsername = item instanceof CredentialItem.Username;
      boolean isPassword = item instanceof CredentialItem.Password;
      if (!isUsername && !isPassword)
        return false;
    }
    return true;
  }

  @Override
  public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem
  {
    if (auth == null)
    {
      try
      {
        auth = Authenticator.requestPasswordAuthentication(host, addr, port, protocol, prompt, sheme);
      }
      catch (Exception pE)
      {
        // ignored
      }
    }
    if (auth == null)
      return false;
    authUtil.auth(auth);

    for (CredentialItem item : items)
    {
      if (item instanceof CredentialItem.Username)
        ((CredentialItem.Username) item).setValue(auth.getUserName());
      else if (item instanceof CredentialItem.Password)
        ((CredentialItem.Password) item).setValue(auth.getPassword());
      else
        throw new UnsupportedCredentialItem(uri, item.getClass().getName() + ":" + item.getPromptText());
    }
    return true;
  }

  @Override
  public void reset(URIish uri)
  {
    auth = null;
  }
}
