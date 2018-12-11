package de.adito.git.gui.actions;

import com.google.inject.*;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.*;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class ActionProvider implements IActionProvider
{

  private final IActionFactory actionFactory;

  @Inject
  ActionProvider(IActionFactory actionFactory)
  {
    this.actionFactory = actionFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MergeAction getMergeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pTargetBranch)
  {
    return actionFactory.createMergeAction(pRepository, pTargetBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommitAction getCommitAction(Observable<Optional<IRepository>> pRepository,
                                      Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createCommitAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DiffToHeadAction getDiffToHeadAction(Observable<Optional<IRepository>> pRepository,
                                              Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createDiffAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IgnoreAction getIgnoreAction(Observable<Optional<IRepository>> pRepository,
                                      Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createIgnoreAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExcludeAction getExcludeAction(Observable<Optional<IRepository>> pRepository,
                                        Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createExcludeAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckoutAction getCheckoutAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranch)
  {
    return actionFactory.createCheckoutAction(pRepository, pBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NewBranchAction getNewBranchAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createNewBranchAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PullAction getPullAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createPullAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PushAction getPushAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createPushAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ShowAllBranchesAction getShowAllBranchesAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowAllBranchesAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ShowCommitsForBranchesAction getShowAllCommitsForBranchAction(Observable<Optional<IRepository>> pRepository,
                                                                       Observable<Optional<List<IBranch>>> pBranches)
  {
    return actionFactory.createShowAllCommitsForBranchAction(pRepository, pBranches);
  }

  @Override
  public Action getShowAllCommitsAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowAllCommitsAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getRevertWorkDirAction(Observable<Optional<IRepository>> pRepository,
                                       Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createRevertWorkDirAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getResetAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pCommittedFilesObservable)
  {
    return actionFactory.createResetAction(pRepository, pCommittedFilesObservable);
  }

  @Override
  public Action getShowStatusWindowAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowStatusWindowAction(pRepository);
  }

}
