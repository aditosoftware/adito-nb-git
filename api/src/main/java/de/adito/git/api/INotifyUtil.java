package de.adito.git.api;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionListener;

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

  /**
   * Shows a simple balloon to display a pMessage to the user. Invokes the passed action on click if the action is non-null
   *
   * @param pTitle          Title
   * @param pMessage        Message
   * @param pAutoDispose    <tt>true</tt> if the balloon should dispose automatically after a couple of seconds
   * @param pActionListener Action to invoke if the user clicks on the notification text
   */
  void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener);

  /**
   * Shows a simple balloon to display a pMessage to the user.
   *
   * @param pEx          Exception that occurred
   * @param pMessage     Message
   * @param pAutoDispose <tt>true</tt> if the balloon should dispose automatically after a couple of seconds
   */
  void notify(@NonNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose);

  /**
   * Shows a simple balloon to display a pMessage to the user. Invokes the passed action on click if the action is non-null
   *
   * @param pEx             Exception that occurred
   * @param pMessage        Message
   * @param pAutoDispose    <tt>true</tt> if the balloon should dispose automatically after a couple of seconds
   * @param pActionListener Action to invoke if the user clicks on the notification text
   */
  void notify(@NonNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener);

}
