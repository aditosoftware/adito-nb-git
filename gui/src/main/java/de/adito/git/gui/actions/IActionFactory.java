package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;

import java.io.File;
import java.util.List;
import java.util.Optional;

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

  NewBranchAction createNewBranchAction(Observable<Optional<IRepository>> pRepository);

  PullAction createPullAction(Observable<Optional<IRepository>> pRepository);

  PushAction createPushAction(Observable<Optional<IRepository>> pRepository);

  ShowAllBranchesAction createShowAllBranchesAction(Observable<Optional<IRepository>> pRepository);

  ShowCommitsForBranchesAction createShowAllCommitsForBranchAction(Observable<Optional<IRepository>> pRepository,
                                                                   Observable<Optional<List<IBranch>>> pBranches);

  ShowAllCommitsAction createShowAllCommitsAction(Observable<Optional<IRepository>> pRepository);

  RevertWorkDirAction createRevertWorkDirAction(Observable<Optional<IRepository>> pRepository,
                                                Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  ResetAction createResetAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  ShowStatusWindowAction createShowStatusWindowAction(Observable<Optional<IRepository>> pRepository);

  ResolveConflictsAction createResolveConflictsAction(Observable<Optional<IRepository>> pRepository,
                                                      Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  GitConfigAction createGitConfigAction(Observable<Optional<IRepository>> pRepository);

  AddTagAction createAddTagAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  DeleteTagAction createDeleteTagAction(Observable<Optional<IRepository>> pRepository, ITag pTag);

  DiffCommitsAction createDiffCommitsAction(Observable<Optional<IRepository>> pRepository,
                                            Observable<Optional<List<ICommit>>> pSelectedCommitsObservable,
                                            Observable<Optional<String>> pSelectedFile);

  DiffCommitToHeadAction createDiffCommitToHeadAction(Observable<Optional<IRepository>> pRepository,
                                                      Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                                      Observable<Optional<String>> pSelectedFile);

  OpenFileAction createOpenFileAction(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  OpenFileStringAction createOpenFileStringAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<String>> pSelectedFile);

  RefreshContentAction createRefreshContentAction(Runnable pRefreshContentCallBack);

  RefreshStatusAction createRefreshStatusAction(Observable<Optional<IRepository>> pRepository);

  FileHistoryAction createShowCommitsForFileAction(Observable<Optional<IRepository>> pRepository, Observable<List<File>> pFile);

  CherryPickAction createCherryPickAction(Observable<Optional<IRepository>> pRepository,
                                          Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  StashChangesAction createStashChangesAction(Observable<Optional<IRepository>> pRepository);

  UnStashChangesAction createUnStashChangesAction(Observable<Optional<IRepository>> pRepository);

  DeleteStashCommitAction createDeleteStashedCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<String>> pCommitId);
}
