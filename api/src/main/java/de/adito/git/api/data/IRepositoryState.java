package de.adito.git.api.data;

import de.adito.git.api.IRepository;

/**
 * Represents the current state of the repository with current Branch and current Status
 *
 * @author m.kaspera, 21.03.2019
 */
public interface IRepositoryState
{

  /**
   * get the branch that the user is currently on
   *
   * @return the current branch
   */
  IBranch getCurrentBranch();

  /**
   * get the state that the repository is in (i.e. REBASING or SAFE)
   *
   * @return current State of the repository
   */
  IRepository.State getState();

}
