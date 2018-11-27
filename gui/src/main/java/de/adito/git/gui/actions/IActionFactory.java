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
interface IActionFactory {

    MergeAction createMergeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pTargetBranch);

    CommitAction createCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    DiffAction createDiffAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    IgnoreAction createIgnoreAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    AddAction createAddAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    ExcludeAction createExcludeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    CheckoutAction createCheckoutAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranch);

    NewBranchAction createNewBranchAction(Observable<Optional<IRepository>> pRepository);

    PullAction createPullAction(Observable<Optional<IRepository>> pRepository, String pTargetId);

    PushAction createPushAction(Observable<Optional<IRepository>> pRepository);

    ShowAllBranchesAction createShowAllBranchesAction(Observable<Optional<IRepository>> pRepository);

    ShowCommitsForBranchesAction createShowAllCommitsAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IBranch>>> pBranches);

    RevertWorkDirAction createRevertWorkDirAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    ResetFilesAction createResetFilesAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    ResetAction createResetAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitsObservable);

    ShowStatusWindowAction createShowStatusWindowAction(Observable<Optional<IRepository>> pRepository);
}
