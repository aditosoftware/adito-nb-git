package de.adito.git.impl.data;

import de.adito.git.api.data.ICherryPickResult;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IMergeDiff;
import org.eclipse.jgit.api.CherryPickResult;

import java.util.List;

/**
 * @author m.kaspera, 11.02.2019
 */
public class CherryPickResultImpl implements ICherryPickResult
{

  private final CherryPickResult result;
  private final ICommit cherryPickHead;
  private final List<IMergeDiff> conflicts;

  public CherryPickResultImpl(CherryPickResult pResult, ICommit pCherryPickHead, List<IMergeDiff> pConflicts)
  {
    result = pResult;
    cherryPickHead = pCherryPickHead;
    conflicts = pConflicts;
  }

  @Override
  public ResultType getResult()
  {
    if (result.getStatus() == CherryPickResult.CherryPickStatus.CONFLICTING)
      return ResultType.CONFLICTING;
    else if (result.getStatus() == CherryPickResult.CherryPickStatus.FAILED)
      return ResultType.FAILED;
    else
      return ResultType.OK;
  }

  @Override
  public ICommit getCherryPickHead()
  {
    return cherryPickHead;
  }

  @Override
  public List<IMergeDiff> getConflicts()
  {
    return conflicts;
  }
}
