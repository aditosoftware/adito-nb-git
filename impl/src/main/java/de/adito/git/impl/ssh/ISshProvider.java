package de.adito.git.impl.ssh;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;

/**
 * @author m.kaspera, 21.12.2018
 */
public interface ISshProvider
{

  TransportConfigCallback getTransportConfigCallBack(Git pGit);

  GitUserInfo getUserInfo(String pPassphrase, String pPassword);

}
