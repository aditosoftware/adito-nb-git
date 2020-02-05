package de.adito.git.api.exception;

/**
 * Zeigt an, dass der User die Authentifizierung unterbrochen hat
 *
 * @author m.kaspera, 05.02.2020
 */
public class AuthCancelledException extends AditoGitException
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public AuthCancelledException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public AuthCancelledException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public AuthCancelledException(Exception pE)
  {
    super(pE);
  }

}
