package de.adito.git.api.exception;

/**
 * Exception that is thrown if no stash commit id was given and several commits are stashed
 * (and it is thus unclear which stashed commit should be used)
 *
 * @author m.kaspera, 14.12.2018
 */
public class AmbiguousStashCommitsException extends AditoGitException
{
  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public AmbiguousStashCommitsException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public AmbiguousStashCommitsException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public AmbiguousStashCommitsException(Exception pE)
  {
    super(pE);
  }
}
