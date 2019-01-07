package de.adito.git.impl.ssh;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.IConfig;

import javax.annotation.Nullable;

/**
 * @author m.kaspera, 21.12.2018
 */
interface ISshFactory
{
  GitUserInfo createUserInfo(@Nullable @Assisted("passphrase") String pPassword, @Nullable @Assisted("password") String pPassphrase);

  TransportConfigCallbackImpl createTransportConfigCallBack(IConfig pConfig);

}
