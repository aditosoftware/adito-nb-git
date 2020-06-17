package de.adito.git.impl.data;

import de.adito.git.api.data.IRemote;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implementation of IRemote, only stores the read information and is not guaranteed to be up to date with the state of the remote as written in the config
 *
 * @author m.kaspera, 17.06.2020
 */
public class RemoteImpl implements IRemote
{

  private final String name;
  private final String url;
  private final String fetchInfo;

  public RemoteImpl(@NotNull String pName, @NotNull String pUrl, @NotNull String pFetchInfo)
  {
    name = pName;
    url = pUrl;
    fetchInfo = pFetchInfo;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getUrl()
  {
    return url;
  }

  @Override
  public String getFetchInfo()
  {
    return fetchInfo;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    RemoteImpl remote = (RemoteImpl) pO;
    return name.equals(remote.name) &&
        url.equals(remote.url) &&
        Objects.equals(fetchInfo, remote.fetchInfo);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(name, url, fetchInfo);
  }
}
