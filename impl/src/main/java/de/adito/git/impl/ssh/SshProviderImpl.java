package de.adito.git.impl.ssh;

import com.google.inject.Inject;
import de.adito.git.api.data.IConfig;

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
  public TransportConfigCallbackImpl getTransportConfigCallBack(IConfig pConfig)
  {
    return factory.createTransportConfigCallBack(pConfig);
  }

  @Override
  public GitUserInfo getUserInfo(String pPassphrase, String pPassword)
  {
    return factory.createUserInfo(pPassphrase, pPassword);
  }
}
