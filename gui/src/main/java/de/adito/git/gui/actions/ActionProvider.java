package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.tree.models.ObservableTreeUpdater;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

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
  public MergeAction getMergeAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<IBranch>> pTargetBranch)
  {
    return actionFactory.createMergeAction(pRepository, pTargetBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommitAction getCommitAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                      @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, String pMessageTemplate)
  {
    return actionFactory.createCommitAction(pRepository, pSelectedFilesObservable, pMessageTemplate);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DiffToHeadAction getDiffToHeadAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                              @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createDiffAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IgnoreAction getIgnoreAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                      @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createIgnoreAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExcludeAction getExcludeAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                        @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createExcludeAction(pRepository, pSelectedFilesObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckoutAction getCheckoutAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<IBranch>> pBranch)
  {
    return actionFactory.createCheckoutAction(pRepository, pBranch);
  }

  @Override
  public Action getCheckoutCommitAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createCheckoutCommitAction(pRepository, pSelectedCommitObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NewBranchAction getNewBranchAction(@NotNull Observable<Optional<IRepository>> pRepository)
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
  public Action getDeleteBranchAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<IBranch>> pSelectedBranch)
  {
    return actionFactory.createDeleteBranchAction(pRepository, pSelectedBranch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PullAction getPullAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createPullAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PushAction getPushAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createPushAction(pRepository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ShowCommitsForBranchesAction getShowAllCommitsForBranchAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                       @NotNull Observable<Optional<List<IBranch>>> pBranches)
  {
    return actionFactory.createShowAllCommitsForBranchAction(pRepository, pBranches);
  }

  @Override
  public Action getShowAllCommitsAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowAllCommitsAction(pRepository);
  }

  @Override
  public Action getShowCommitsForFileAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<List<File>> pFile)
  {
    return actionFactory.createShowCommitsForFileAction(pRepository, pFile);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getRevertWorkDirAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                       @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createRevertWorkDirAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getRevertCommitsAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitsObservable)
  {
    return actionFactory.createRevertCommitsAction(pRepository, pSelectedCommitsObservable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Action getResetAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pCommittedFilesObservable)
  {
    return actionFactory.createResetAction(pRepository, pCommittedFilesObservable);
  }

  @Override
  public Action getShowStatusWindowAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowStatusWindowAction(pRepository);
  }

  @Override
  public Action getResolveConflictsAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                          @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createResolveConflictsAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getGitConfigAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createGitConfigAction(pRepository);
  }

  @Override
  public Action getAddTagAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createAddTagAction(pRepository, pSelectedCommitObservable);
  }

  @Override
  public Action getDeleteSpecificTagAction(@NotNull Observable<Optional<IRepository>> pRepository, ITag pTag)
  {
    return actionFactory.createDeleteSpecificTagAction(pRepository, pTag);
  }

  @Override
  public Action getDeleteTagAction(@NotNull Observable<Optional<IRepository>> pRepository, Observable<Optional<ITag>> pTagObservable)
  {
    return actionFactory.createDeleteTagAction(pRepository, pTagObservable);
  }

  @Override
  public Action getDiffCommitsAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                     @NotNull Observable<Optional<ICommit>> pParentCommit,
                                     @NotNull Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createDiffCommitsAction(pRepository, pSelectedCommitObservable, pParentCommit, pSelectedFile);
  }

  @Override
  public Action getDiffCommitToHeadAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                          @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                          @NotNull Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createDiffCommitToHeadAction(pRepository, pSelectedCommitObservable, pSelectedFile);
  }

  @Override
  public Action getOpenFileAction(@NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createOpenFileAction(pSelectedFilesObservable);
  }

  @Override
  public Action getOpenFileStringAction(@NotNull Observable<Optional<String>> pSelectedFile)
  {
    return actionFactory.createOpenFileStringAction(pSelectedFile);
  }

  @Override
  public Action getRefreshContentAction(@NotNull Runnable pRefreshContentCallBack)
  {
    return actionFactory.createRefreshContentAction(pRefreshContentCallBack);
  }

  @Override
  public Action getRefreshStatusAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Runnable pRefreshTree)
  {
    return actionFactory.createRefreshStatusAction(pRepository, pRefreshTree);
  }

  @Override
  public Action getCherryPickAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return actionFactory.createCherryPickAction(pRepository, pSelectedCommitObservable);
  }

  @Override
  public Action getStashChangesAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createStashChangesAction(pRepository);
  }

  @Override
  public Action getUnStashChangesAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createUnStashChangesAction(pRepository);
  }

  @Override
  public Action getDeleteStashedCommitAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<String>> pCommitId)
  {
    return actionFactory.createDeleteStashedCommitAction(pRepository, pCommitId);
  }

  @Override
  public Action getFetchAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createFetchAction(pRepository);
  }

  @Override
  public Action getCollapseTreeAction(@NotNull JTree pTree)
  {
    return actionFactory.createCollapseTreeAction(pTree);
  }

  @Override
  public Action getExpandTreeAction(@NotNull JTree pTree)
  {
    return actionFactory.createExpandTreeAction(pTree);
  }

  @Override
  public Action getShowTagWindowAction(@NotNull Consumer<ICommit> pSelectedCommitCallback, @NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createShowTagWindowAction(pSelectedCommitCallback, pRepository);
  }

  @Override
  public Action getSwitchTreeViewAction(@NotNull JTree pTree, @NotNull File pProjectDirectory, @NotNull String pCallerName,
                                        @NotNull ObservableTreeUpdater<IFileChangeType> pObservableTreeUpdater)
  {
    return actionFactory.createSwitchTreeViewAction(pTree, pProjectDirectory, pCallerName, pObservableTreeUpdater);
  }

  @Override
  public Action getSwitchDiffTreeViewAction(@NotNull JTree pTree, @NotNull ObservableTreeUpdater<IDiffInfo> pObservableTreeUpdater, @NotNull File pProjectDirectory,
                                            @NotNull String pCallerName)
  {
    return actionFactory.createSwitchDiffTreeViewAction(pTree, pObservableTreeUpdater, pProjectDirectory, pCallerName);
  }

  @Override
  public Action getCreatePatchAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable)
  {
    return actionFactory.createPatchAction(pRepository, pSelectedFilesObservable);
  }

  @Override
  public Action getApplyPatchAction(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return actionFactory.createApplyPatchAction(pRepository);
  }

}
