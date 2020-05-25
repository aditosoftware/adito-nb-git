package de.adito.git.api.exception;

/**
 * @author m.kaspera, 25.05.2020
 */
public class PushRejectedOtherReasonException extends AditoGitException
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public PushRejectedOtherReasonException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public PushRejectedOtherReasonException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public PushRejectedOtherReasonException(Exception pE)
  {
    super(pE);
  }
}
