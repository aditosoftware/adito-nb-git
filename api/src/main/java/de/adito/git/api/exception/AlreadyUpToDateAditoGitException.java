package de.adito.git.api.exception;

/**
 * Exception that is thrown if two branches that should be merged are already merged/up-to-date, or any other situation where a branch/branches are already up-to-date
 * This is mostly done so that the gui can tell the user about that fact, and less about representing a true error state
 *
 * @author m.kaspera, 12.11.2019
 */
public class AlreadyUpToDateAditoGitException extends AditoGitException
{

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public AlreadyUpToDateAditoGitException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public AlreadyUpToDateAditoGitException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public AlreadyUpToDateAditoGitException(Exception pE)
  {
    super(pE);
  }
}
