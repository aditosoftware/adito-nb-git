package de.adito.git.api.exception;

/**
 * Thrown if no matching remote repository can be found for a given URI
 *
 * @author m.kaspera, 05.02.2020
 */
public class UnknownRemoteRepositoryException extends AditoGitException
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public UnknownRemoteRepositoryException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public UnknownRemoteRepositoryException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public UnknownRemoteRepositoryException(Exception pE)
  {
    super(pE);
  }

}
