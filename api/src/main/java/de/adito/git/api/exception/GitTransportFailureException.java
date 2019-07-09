package de.adito.git.api.exception;

/**
 * Exception thrown when any error occurs during transport (e.g. missing rights to create a branch, missing credentials)
 *
 * @author m.kaspera, 09.07.2019
 */
public class GitTransportFailureException extends AditoGitException
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public GitTransportFailureException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public GitTransportFailureException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public GitTransportFailureException(Exception pE)
  {
    super(pE);
  }
}
