package de.adito.git.impl.data;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IRepositoryState;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Implements the IRepositoryState Interface and provides the current branch/state
 *
 * @author m.kaspera, 21.03.2019
 */
public class RepositoryStateImpl implements IRepositoryState
{

  private final Set<IRepository.State> commitAbleStates = Set.of(IRepository.State.SAFE, IRepository.State.MERGING_RESOLVED, IRepository.State.CHERRY_PICKING_RESOLVED,
                                                                 IRepository.State.REVERTING_RESOLVED, IRepository.State.REBASING, IRepository.State.REBASING_REBASING,
                                                                 IRepository.State.APPLY, IRepository.State.REBASING_MERGE, IRepository.State.REBASING_INTERACTIVE);
  private final Set<IRepository.State> resetAbleStates = Set.of(IRepository.State.SAFE, IRepository.State.MERGING, IRepository.State.MERGING_RESOLVED,
                                                                IRepository.State.CHERRY_PICKING, IRepository.State.CHERRY_PICKING_RESOLVED, IRepository.State.REVERTING,
                                                                IRepository.State.REVERTING_RESOLVED);
  private final Set<IRepository.State> amendAbleStates = Set.of(IRepository.State.SAFE, IRepository.State.REBASING, IRepository.State.REBASING_REBASING,
                                                                IRepository.State.APPLY, IRepository.State.REBASING_MERGE, IRepository.State.REBASING_INTERACTIVE);
  private final Set<IRepository.State> checkoutAbleStates = Set.of(IRepository.State.SAFE, IRepository.State.MERGING_RESOLVED, IRepository.State.CHERRY_PICKING_RESOLVED,
                                                                   IRepository.State.REVERTING_RESOLVED);
  private final IBranch currentBranch;
  private final IBranch currentRemoteTrackedBranch;
  private final IRepository.State currentState;
  private final List<String> remotes;

  public RepositoryStateImpl(@NonNull IBranch pCurrentBranch, @Nullable IBranch pCurrentRemoteTrackedBranch, @NonNull IRepository.State pCurrentState,
                             @NonNull List<String> pRemotes)
  {
    currentBranch = pCurrentBranch;
    currentRemoteTrackedBranch = pCurrentRemoteTrackedBranch;
    currentState = pCurrentState;
    remotes = pRemotes;
  }

  @NonNull
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

  @NonNull
  @Override
  public IRepository.State getState()
  {
    return currentState;
  }

  @Override
  public boolean canCommit()
  {
    return commitAbleStates.contains(currentState);
  }

  @Override
  public boolean canResetHead()
  {
    return resetAbleStates.contains(currentState);
  }

  @Override
  public boolean canAmend()
  {
    return amendAbleStates.contains(currentState);
  }

  @Override
  public boolean canCheckout()
  {
    return checkoutAbleStates.contains(currentState);
  }

  @Override
  public @NonNull List<String> getRemotes()
  {
    return remotes;
  }
}
