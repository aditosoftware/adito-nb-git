package de.adito.git.gui.actions;

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
interface IActionFactory
{

  MergeAction createMergeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pTargetBranch);

  CommitAction createCommitAction(Observable<Optional<IRepository>> pRepository,
                                  Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable, String pMessageTemplate);

  DiffToHeadAction createDiffAction(Observable<Optional<IRepository>> pRepository,
                                    Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  IgnoreAction createIgnoreAction(Observable<Optional<IRepository>> pRepository,
                                  Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  ExcludeAction createExcludeAction(Observable<Optional<IRepository>> pRepository,
                                    Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  CheckoutAction createCheckoutAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranch);

  CheckoutCommitAction createCheckoutCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitObservable);

  NewBranchAction createNewBranchAction(Observable<Optional<IRepository>> pRepository);

  NewBranchFromCommitAction createNewBranchAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pStartingPoint);

  DeleteBranchAction createDeleteBranchAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pSelectedBranch);

  PullAction createPullAction(Observable<Optional<IRepository>> pRepository);

  PushAction createPushAction(Observable<Optional<IRepository>> pRepository);

  ShowCommitsForBranchesAction createShowAllCommitsForBranchAction(Observable<Optional<IRepository>> pRepository,
                                                                   Observable<Optional<List<IBranch>>> pBranches);

  ShowAllCommitsAction createShowAllCommitsAction(Observable<Optional<IRepository>> pRepository);

  RevertWorkDirAction createRevertWorkDirAction(Observable<Optional<IRepository>> pRepository,
                                                Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);


  RevertCommitsAction createRevertCommitsAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                                @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  ResetAction createResetAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  ShowStatusWindowAction createShowStatusWindowAction(Observable<Optional<IRepository>> pRepository);

  ResolveConflictsAction createResolveConflictsAction(Observable<Optional<IRepository>> pRepository,
                                                      Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  GitConfigAction createGitConfigAction(Observable<Optional<IRepository>> pRepository);

  AddTagAction createAddTagAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  DeleteSpecificTagAction createDeleteSpecificTagAction(Observable<Optional<IRepository>> pRepository, ITag pTag);

  DeleteTagAction createDeleteTagAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<ITag>> pTagObservable);

  DiffCommitsAction createDiffCommitsAction(Observable<Optional<IRepository>> pRepository,
                                            Observable<Optional<List<ICommit>>> pSelectedCommitsObservable,
                                            Observable<Optional<ICommit>> pParentCommit,
                                            Observable<Optional<String>> pSelectedFile);

  DiffCommitToHeadAction createDiffCommitToHeadAction(Observable<Optional<IRepository>> pRepository,
                                                      Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                                      Observable<Optional<String>> pSelectedFile);

  OpenFileAction createOpenFileAction(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  OpenFileStringAction createOpenFileStringAction(Observable<Optional<String>> pSelectedFile);

  RefreshContentAction createRefreshContentAction(Runnable pRefreshContentCallBack);

  RefreshStatusAction createRefreshStatusAction(Observable<Optional<IRepository>> pRepository, Runnable pRefreshTree);

  FileHistoryAction createShowCommitsForFileAction(Observable<Optional<IRepository>> pRepository, Observable<List<File>> pFile);

  CherryPickAction createCherryPickAction(Observable<Optional<IRepository>> pRepository,
                                          Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  StashChangesAction createStashChangesAction(Observable<Optional<IRepository>> pRepository);

  UnStashChangesAction createUnStashChangesAction(Observable<Optional<IRepository>> pRepository);

  DeleteStashCommitAction createDeleteStashedCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<String>> pCommitId);

  FetchAction createFetchAction(Observable<Optional<IRepository>> pRepository);

  CollapseTreeAction createCollapseTreeAction(JTree pTree);

  ExpandTreeAction createExpandTreeAction(JTree pTree);

  ShowTagWindowAction createShowTagWindowAction(Consumer<ICommit> pSelectedCommitCallback, Observable<Optional<IRepository>> pRepository);

  SwitchTreeViewAction createSwitchTreeViewAction(@NotNull JTree pTree, @NotNull File pProjectDirectory, @NotNull String pCallerName,
                                                  @NotNull ObservableTreeUpdater<IFileChangeType> pObservableTreeUpdater);

  SwitchDiffTreeViewAction createSwitchDiffTreeViewAction(@NotNull JTree pTree, @NotNull ObservableTreeUpdater<IDiffInfo> pObservableTreeUpdater,
                                                          @NotNull File pProjectDirectory, @NotNull String pCallerName);

  CreatePatchAction createPatchAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                      @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  ApplyPatchAction createApplyPatchAction(@NotNull Observable<Optional<IRepository>> pRepository);
}
