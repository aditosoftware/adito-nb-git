package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Optional;

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
                                      Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, String pMessageTemplate)
  {
    return actionFactory.createCommitAction(pRepository, pSelectedFilesObservable, pMessageTemplate);
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

  @Override
  public Action getDeleteBranchAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pSelectedBranch)
  {
    return actionFactory.createDeleteBranchAction(pRepository, pSelectedBranch);
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

  @Override
  public Action getShowCommitsForFileAction(Observable<Optional<IRepository>> pRepository, Observable<List<File>> pFile)
  {
    return actionFactory.createShowCommitsForFileAction(pRepository, pFile);
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

  @Override
  public Action getResolveConflictsAction(Observable<Optional<IRepository>> pRepository,
                                          Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createResolveConflictsAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getGitConfigAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createGitConfigAction(pRepository);
  }

  @Override
  public Action getAddTagAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createAddTagAction(pRepository, pSelectedCommitObservable);
  }

  @Override
  public Action getDeleteTagAction(Observable<Optional<IRepository>> pRepository, ITag pTag)
  {
    return actionFactory.createDeleteTagAction(pRepository, pTag);
  }

  @Override
  public Action getDiffCommitsAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                     Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createDiffCommitsAction(pRepository, pSelectedCommitObservable, pSelectedFile);
  }

  @Override
  public Action getDiffCommitToHeadAction(Observable<Optional<IRepository>> pRepository,
                                          Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                          Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createDiffCommitToHeadAction(pRepository, pSelectedCommitObservable, pSelectedFile);
  }

  @Override
  public Action getOpenFileAction(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createOpenFileAction(pSelectedFilesObservable);
  }

  @Override
  public Action getOpenFileStringAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createOpenFileStringAction(pRepository, pSelectedFile);
  }

  @Override
  public Action getRefreshContentAction(Runnable pRefreshContentCallBack)
  {
    return actionFactory.createRefreshContentAction(pRefreshContentCallBack);
  }

  @Override
  public Action getRefreshStatusAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createRefreshStatusAction(pRepository);
  }

  @Override
  public Action getCherryPickAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createCherryPickAction(pRepository, pSelectedCommitObservable);
  }

  @Override
  public Action getStashChangesAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createStashChangesAction(pRepository);
  }

  @Override
  public Action getUnStashChangesAction(Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createUnStashChangesAction(pRepository);
  }

  @Override
  public Action getDeleteStashedCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<String>> pCommitId)
  {
    return actionFactory.createDeleteStashedCommitAction(pRepository, pCommitId);
  }

}
