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
   * Checks if the current State of the repository allows the user to do a commit
   *
   * @return true if the user can do a commit
   */
  boolean canCommit();

  /**
   * Checks if the current State of the repository allows the user to reset the HEAD
   *
   * @return true if the user can reset the HEAD
   */
  boolean canResetHead();

  /**
   * Checks if the current State of the repository allows the user to do an amended commit
   *
   * @return true if the user can do an amended commit
   */
  boolean canAmend();

  /**
   * Checks if the current State of the repository allows the user to check out another commit
   *
   * @return true if the user can check out another commit
   */
  boolean canCheckout();

  /**
   * retrieve the names of the configured remotes for this repository
   *
   * @return names of the remotes stored in the config
   */
  @NotNull
  List<String> getRemotes();

}
