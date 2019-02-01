package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 26.10.2018
 */
interface IActionFactory
{

  MergeAction createMergeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pTargetBranch);

  CommitAction createCommitAction(Observable<Optional<IRepository>> pRepository,
                                  Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

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

  DiffCommitsAction createDiffCommitsAction(Observable<Optional<IRepository>> pRepository,
                                            Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

  OpenFileAction createOpenFileAction(Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);
}
