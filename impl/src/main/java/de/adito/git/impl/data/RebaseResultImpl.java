package de.adito.git.impl.data;

import de.adito.git.api.data.IRebaseResult;
import de.adito.git.api.data.diff.IMergeData;
import org.eclipse.jgit.api.RebaseResult;

import java.util.List;

/**
 * Class that keeps the result of a Rebase operation
 *
 * @author Michael Kaspera 05.12.2018
 */
public class RebaseResultImpl implements IRebaseResult
{

  private final List<IMergeData> conflicts;
  private final RebaseResult.Status status;

  /**
   * @param pConflicts List with IMergeDatas describing the conflicts
   * @param pStatus    the status of the undertaken Rebase operation
   */
  public RebaseResultImpl(List<IMergeData> pConflicts, RebaseResult.Status pStatus)
  {
    conflicts = pConflicts;
    status = pStatus;
  }

  @Override
  public List<IMergeData> getMergeConflicts()
  {
    return conflicts;
  }

  @Override
  public ResultType getResultType()
  {
    ResultType returnValue;
    switch (status)
    {
      case ABORTED:
        returnValue = ResultType.ABORTED;
        break;
      case STOPPED:
        returnValue = ResultType.STOPPED;
        break;
      case OK:
        returnValue = ResultType.FINISHED;
        break;
      case UP_TO_DATE:
        returnValue = ResultType.UP_TO_DATE;
        break;
      case FAILED:
        returnValue = ResultType.FAILED;
        break;
      case CONFLICTS:
        returnValue = ResultType.EXISTING_CONFLICTS;
        break;
      case FAST_FORWARD:
        returnValue = ResultType.FAST_FORWARD;
        break;
      default:
        returnValue = null;
    }
    return returnValue;
  }

  @Override
  public boolean isSuccess()
  {
    return status.isSuccessful();
  }
}
