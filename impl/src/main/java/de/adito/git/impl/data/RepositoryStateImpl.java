package de.adito.git.impl.data;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IRepositoryState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Implements the IRepositoryState Interface and provides the current branch/state
 *
 * @author m.kaspera, 21.03.2019
 */
public class RepositoryStateImpl implements IRepositoryState
{

  private final IBranch currentBranch;
  private final IBranch currentRemoteTrackedBranch;
  private final IRepository.State currentState;
  private final List<String> remotes;

  public RepositoryStateImpl(@NotNull IBranch pCurrentBranch, @Nullable IBranch pCurrentRemoteTrackedBranch, @NotNull IRepository.State pCurrentState,
                             @NotNull List<String> pRemotes)
  {
    currentBranch = pCurrentBranch;
    currentRemoteTrackedBranch = pCurrentRemoteTrackedBranch;
    currentState = pCurrentState;
    remotes = pRemotes;
  }

  @NotNull
  @Override
  public IBranch getCurrentBranch()
  {
    return currentBranch;
  }

  @Override
  public @Nullable IBranch getCurrentRemoteTrackedBranch()
  {
    return currentRemoteTrackedBranch;
  }

  @NotNull
  @Override
  public IRepository.State getState()
  {
    return currentState;
  }

  @Override
  public @NotNull List<String> getRemotes()
  {
    return remotes;
  }
}
