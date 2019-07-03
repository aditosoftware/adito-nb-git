package de.adito.git.api.exception;

/**
 * Exception that is thrown if a remote tracked branch would have been necessary for an operation, but none was configured for the current branch
 *
 * @author m.kaspera, 03.07.2019
 */
public class MissingTrackedBranchException extends AditoGitException
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public MissingTrackedBranchException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public MissingTrackedBranchException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public MissingTrackedBranchException(Exception pE)
  {
    super(pE);
  }
}
