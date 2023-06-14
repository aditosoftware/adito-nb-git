package de.adito.git.api.data;

import lombok.NonNull;

/**
 * Represents an object containing all information about a remote
 *
 * @author m.kaspera, 17.06.2020
 */
public interface IRemote
{

  /**
   * get the name of the remote
   *
   * @return name of the remote
   */
  String getName();

  /**
   * get the url associated with the remote
   *
   * @return url associated with the remote
   */
  String getUrl();

  /**
   * get the fetch info string associated with the remote
   *
   * @return fetch info string associated with the remote
   */
  String getFetchInfo();

  /**
   * Construct the string for the fetch = part of the remote config based on the remote name
   *
   * @param pRemoteName name of the remote
   * @return String for the fetchInfo
   */
  static String getFetchStringFromName(@NonNull String pRemoteName)
  {
    return "+refs/heads/*:refs/remotes/" + pRemoteName + "/*";
  }

}
