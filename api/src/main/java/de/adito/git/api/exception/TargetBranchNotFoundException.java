package de.adito.git.api.exception;

import de.adito.git.api.data.ICommit;
import org.jetbrains.annotations.Nullable;

/**
 * Exception that is throw when the target branch of a conflict case cannot be determined
 *
 * @author m.kaspera, 08.03.2019
 */
public class TargetBranchNotFoundException extends AditoGitException
{

  private transient final ICommit origHead;

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   */
  public TargetBranchNotFoundException(String pMessage)
  {
    super(pMessage);
    origHead = null;
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage Message to throw further up
   * @param pE       Exception causing this exception to be thrown
   */
  public TargetBranchNotFoundException(String pMessage, Exception pE)
  {
    super(pMessage, pE);
    origHead = null;
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE Exception causing this exception to be thrown
   */
  public TargetBranchNotFoundException(Exception pE)
  {
    super(pE);
    origHead = null;
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage  Message to throw further up
   * @param pOrigHead The commit the head was pointing to before a dangerous operation
   */
  public TargetBranchNotFoundException(String pMessage, ICommit pOrigHead)
  {
    super(pMessage);
    origHead = pOrigHead;
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pMessage  Message to throw further up
   * @param pE        Exception causing this exception to be thrown
   * @param pOrigHead The commit the head was pointing to before a dangerous operation
   */
  public TargetBranchNotFoundException(String pMessage, Exception pE, ICommit pOrigHead)
  {
    super(pMessage, pE);
    origHead = pOrigHead;
  }

  /**
   * Default constructor for Exceptions
   *
   * @param pE        Exception causing this exception to be thrown
   * @param pOrigHead The commit the head was pointing to before a dangerous operation
   */
  public TargetBranchNotFoundException(Exception pE, ICommit pOrigHead)
  {
    super(pE);
    origHead = pOrigHead;
  }

  /**
   * @return The commit the head was pointing to before a dangerous operation
   */
  @Nullable
  public ICommit getOrigHead()
  {
    return origHead;
  }
}
