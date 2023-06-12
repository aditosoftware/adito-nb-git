package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.tree.models.ObservableTreeUpdater;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
  public MergeAction getMergeAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<IBranch>> pTargetBranch)
  {
    return actionFactory.createMergeAction(pRepository, pTargetBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MergeRemoteAction getMergeRemoteAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<IBranch>> pTargetBranch)
  {
    return actionFactory.createMergeRemoteAction(pRepository, pTargetBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommitAction getCommitAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                      @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, String pMessageTemplate)
  {
    return actionFactory.createCommitAction(pRepository, pSelectedFilesObservable, pMessageTemplate);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DiffToHeadAction getDiffToHeadAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                              @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable,
                                              @NonNull Boolean pIsAsync)
  {
    return actionFactory.createDiffAction(pRepository, pSelectedFilesObservable, pIsAsync);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IgnoreAction getIgnoreAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                      @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createIgnoreAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExcludeAction getExcludeAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                        @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createExcludeAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckoutAction getCheckoutAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<IBranch>> pBranch)
  {
    return actionFactory.createCheckoutAction(pRepository, pBranch);
  }

  @Override
  public Action getCheckoutCommitAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createCheckoutCommitAction(pRepository, pSelectedCommitObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NewBranchAction getNewBranchAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createNewBranchAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getNewBranchAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pStartingPoint)
  {
    return actionFactory.createNewBranchAction(pRepository, pStartingPoint);
  }

  @Override
  public Action getDeleteBranchAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<IBranch>> pSelectedBranch)
  {
    return actionFactory.createDeleteBranchAction(pRepository, pSelectedBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PullAction getPullAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createPullAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PushAction getPushAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createPushAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ShowCommitsForBranchesAction getShowAllCommitsForBranchAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                                                       @NonNull Observable<Optional<List<IBranch>>> pBranches)
  {
    return actionFactory.createShowAllCommitsForBranchAction(pRepository, pBranches);
  }

  @Override
  public Action getShowAllCommitsAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowAllCommitsAction(pRepository);
  }

  @Override
  public Action getShowCommitsForFileAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<List<File>> pFile)
  {
    return actionFactory.createShowCommitsForFileAction(pRepository, pFile);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getRevertWorkDirAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                       @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createRevertWorkDirAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getRevertCommitsAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitsObservable)
  {
    return actionFactory.createRevertCommitsAction(pRepository, pSelectedCommitsObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getResetAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<ICommit>>> pCommittedFilesObservable)
  {
    return actionFactory.createResetAction(pRepository, pCommittedFilesObservable);
  }

  @Override
  public Action getRenormalizeNewlinesAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createRenormalizeNewlinesAction(pRepository);
  }

  @Override
  public Action getShowStatusWindowAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowStatusWindowAction(pRepository);
  }

  @Override
  public Action getResolveConflictsAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                          @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createResolveConflictsAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getGitConfigAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createGitConfigAction(pRepository);
  }

  @Override
  public Action getAddTagAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createAddTagAction(pRepository, pSelectedCommitObservable);
  }

  @Override
  public Action getDeleteSpecificTagAction(@NonNull Observable<Optional<IRepository>> pRepository, ITag pTag)
  {
    return actionFactory.createDeleteSpecificTagAction(pRepository, pTag);
  }

  @Override
  public Action getDeleteTagAction(@NonNull Observable<Optional<IRepository>> pRepository, Observable<Optional<ITag>> pTagObservable)
  {
    return actionFactory.createDeleteTagAction(pRepository, pTagObservable);
  }

  @Override
  public Action getDiffCommitsAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                     @NonNull Observable<Optional<ICommit>> pParentCommit,
                                     @NonNull Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createDiffCommitsAction(pRepository, pSelectedCommitObservable, pParentCommit, pSelectedFile);
  }

  @Override
  public Action getDiffCommitToHeadAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                          @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                          @NonNull Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createDiffCommitToHeadAction(pRepository, pSelectedCommitObservable, pSelectedFile);
  }

  @Override
  public Action getOpenFileAction(@NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createOpenFileAction(pSelectedFilesObservable);
  }

  @Override
  public Action getOpenFileStringAction(@NonNull Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createOpenFileStringAction(pSelectedFile);
  }

  @Override
  public Action getRefreshContentAction(@NonNull Runnable pRefreshContentCallBack)
  {
    return actionFactory.createRefreshContentAction(pRefreshContentCallBack);
  }

  @Override
  public Action getRefreshStatusAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Runnable pRefreshTree)
  {
    return actionFactory.createRefreshStatusAction(pRepository, pRefreshTree);
  }

  @Override
  public Action getCherryPickAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createCherryPickAction(pRepository, pSelectedCommitObservable);
  }

  @Override
  public Action getStashChangesAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createStashChangesAction(pRepository);
  }

  @Override
  public Action getUnStashChangesAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createUnStashChangesAction(pRepository);
  }

  @Override
  public Action getDeleteStashedCommitAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<String>> pCommitId)
  {
    return actionFactory.createDeleteStashedCommitAction(pRepository, pCommitId);
  }

  @Override
  public Action getFetchAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createFetchAction(pRepository);
  }

  @Override
  public Action getCollapseTreeAction(@NonNull JTree pTree)
  {
    return actionFactory.createCollapseTreeAction(pTree);
  }

  @Override
  public Action getExpandTreeAction(@NonNull JTree pTree)
  {
    return actionFactory.createExpandTreeAction(pTree);
  }

  @Override
  public Action getShowTagWindowAction(@NonNull Consumer<ICommit> pSelectedCommitCallback, @NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowTagWindowAction(pSelectedCommitCallback, pRepository);
  }

  @Override
  public Action getSwitchTreeViewAction(@NonNull JTree pTree, @NonNull File pProjectDirectory, @NonNull String pCallerName,
                                        @NonNull ObservableTreeUpdater<IFileChangeType> pObservableTreeUpdater)
  {
    return actionFactory.createSwitchTreeViewAction(pTree, pProjectDirectory, pCallerName, pObservableTreeUpdater);
  }

  @Override
  public Action getSwitchDiffTreeViewAction(@NonNull JTree pTree, @NonNull ObservableTreeUpdater<IDiffInfo> pObservableTreeUpdater, @NonNull File pProjectDirectory,
                                            @NonNull String pCallerName)
  {
    return actionFactory.createSwitchDiffTreeViewAction(pTree, pObservableTreeUpdater, pProjectDirectory, pCallerName);
  }

  @Override
  public Action getCreatePatchAction(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createPatchAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getApplyPatchAction(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createApplyPatchAction(pRepository);
  }

  @Override
  public Action getMarkResolvedAction(@NonNull Observable<Optional<IRepository>> pRepository,
                                      @NonNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createMarkResolvedAction(pRepository, pSelectedFilesObservable);
  }

}
