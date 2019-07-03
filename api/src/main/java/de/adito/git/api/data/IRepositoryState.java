package de.adito.git.api.data;

import de.adito.git.api.IRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
  @NotNull
  IBranch getCurrentBranch();

  /**
   * retrieves the remote branch that is tracked by the current branch
   *
   * @return the remote branch tracked by the current branch or null if no remote branch is tracked by the current branch
   */
  @Nullable
  IBranch getCurrentRemoteTrackedBranch();

  /**
   * get the state that the repository is in (i.e. REBASING or SAFE)
   *
   * @return current State of the repository
   */
  @NotNull
  IRepository.State getState();

  /**
   * retrieve the names of the configured remotes for this repository
   *
   * @return names of the remotes stored in the config
   */
  @NotNull
  List<String> getRemotes();

}
