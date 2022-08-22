package de.adito.git.api;

import org.jetbrains.annotations.*;

import java.net.PasswordAuthentication;

/**
 * @author p.neub, 19.08.2022
 */
public interface IAuthUtil
{
  /**
   * Should be called once authentication has been performed
   *
   * @param pAuth the authentication details
   */
  void auth(@NotNull PasswordAuthentication pAuth);

  /**
   * Returns the remembered authentication details, if present
   *
   * @return the remembered authentication details
   */
  @Nullable
  PasswordAuthentication getAuth();

  /**
   * Runs the given code and remembers the authentication if needed more than once
   *
   * @param pCode the code that shall be executed
   */
  void reuseAuthIfNeededMoreThanOnce(@NotNull Runnable pCode);
}
