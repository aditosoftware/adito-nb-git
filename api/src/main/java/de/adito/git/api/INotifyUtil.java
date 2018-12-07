package de.adito.git.api;

import org.jetbrains.annotations.Nullable;

/**
 * Interface to display Messages to the user
 *
 * @author m.kaspera 06.12.2018
 */

public interface INotifyUtil
{

  /**
   * Shows a simple balloon to display a pMessage to the user.
   *
   * @param pTitle       Title
   * @param pMessage     Message
   * @param pAutoDispose <tt>true</tt> if the balloon should dispose automatically after a couple of seconds
   */
  void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose);

}
