package de.adito.git.gui.actions;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
interface IActionFactory {

    MergeAction createMergeAction(Observable<IRepository> pRepository, @Assisted("current") String pCurrentBranch, @Assisted("target") String pTargetBranch);

    CommitAction createCommitAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    DiffAction createDiffAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    IgnoreAction createIgnoreAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    AddAction createAddAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    ExcludeAction createExcludeAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    CheckoutAction createCheckoutAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranchList);

    NewBranchAction createNewBranchAction(Observable<IRepository> pRepository);

    PullAction createPullAction(Observable<IRepository> pRepository, String pTargetId);

    PushAction createPushAction(Observable<IRepository> pRepository);

    ShowAllBranchesAction createShowAllBranchesAction(Observable<IRepository> pRepository);

    ShowAllCommitsAction createShowAllCommitsAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranches);

    RevertWorkDirAction createRevertWorkDirAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    ResetFilesAction createResetFilesAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    ResetAction createResetAction(Observable<IRepository> pRepository, Observable<List<ICommit>> pSelectedCommitsObservable);
}
