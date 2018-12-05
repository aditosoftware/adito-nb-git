package de.adito.git.api;

/**
 * Wrapper for the normal Exception so it is obvious from which
 * module the Exception comes from
 *
 * @author Michael Kaspera 05.12.2018
 */
public class AditoGitException extends Exception
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public AditoGitException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public AditoGitException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public AditoGitException(Exception pE)
  {
    super(pE);
  }
}
