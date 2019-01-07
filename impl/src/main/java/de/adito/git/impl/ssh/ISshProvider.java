package de.adito.git.impl.ssh;

import de.adito.git.api.data.IConfig;
import org.eclipse.jgit.api.TransportConfigCallback;

/**
 * @author m.kaspera, 21.12.2018
 */
public interface ISshProvider
{

  TransportConfigCallback getTransportConfigCallBack(IConfig pConfig);

  GitUserInfo getUserInfo(String pPassphrase, String pPassword);

}
