package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import org.eclipse.jgit.api.Git;

/**
 * @author m.kaspera, 21.12.2018
 */
class SshProviderImpl implements ISshProvider
{

  private final ISshFactory factory;

  @Inject
  SshProviderImpl(ISshFactory pFactory)
  {
    factory = pFactory;
  }

  @Override
  public TransportConfigCallbackImpl getTransportConfigCallBack(Git pGit)
  {
    return factory.createTransportConfigCallBack(pGit);
  }

  @Override
  public GitUserInfo getUserInfo(String pPassphrase, String pPassword)
  {
    return factory.createUserInfo(pPassphrase, pPassword);
  }
}
