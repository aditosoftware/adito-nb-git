package de.adito.git.impl.data;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IRepositoryState;

/**
 * Implements the IRepositoryState Interface and provides the current branch/state
 *
 * @author m.kaspera, 21.03.2019
 */
public class RepositoryStateImpl implements IRepositoryState
{

  private final IBranch currentBranch;
  private final IRepository.State currentState;

  public RepositoryStateImpl(IBranch pCurrentBranch, IRepository.State pCurrentState)
  {
    currentBranch = pCurrentBranch;
    currentState = pCurrentState;
  }

  @Override
  public IBranch getCurrentBranch()
  {
    return currentBranch;
  }

  @Override
  public IRepository.State getState()
  {
    return currentState;
  }
}
