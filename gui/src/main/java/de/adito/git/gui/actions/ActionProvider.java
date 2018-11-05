package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class ActionProvider implements IActionProvider {

    private final IActionFactory actionFactory;

    @Inject
    ActionProvider(IActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    @Override
    public MergeAction getMergeAction(Observable<IRepository> pRepository, String pCurrentBranch, String pTargetBranch) {
        return actionFactory.createMergeAction(pRepository, pCurrentBranch, pTargetBranch);
    }

    @Override
    public CommitAction getCommitAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createCommitAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public DiffAction getDiffAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createDiffAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public IgnoreAction getIgnoreAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createIgnoreAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public AddAction getAddAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createAddAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public ExcludeAction getExcludeAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createExcludeAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public CheckoutAction getCheckoutAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranchList) {
        return actionFactory.createCheckoutAction(pRepository, pBranchList);
    }

    @Override
    public NewBranchAction getNewBranchAction(Observable<IRepository> pRepository) {
        return actionFactory.createNewBranchAction(pRepository);
    }

    @Override
    public PullAction getPullAction(Observable<IRepository> pRepository, String pTargetId) {
        return actionFactory.createPullAction(pRepository, pTargetId);
    }

    @Override
    public PushAction getPushAction(Observable<IRepository> pRepository) {
        return actionFactory.createPushAction(pRepository);
    }

    @Override
    public ShowAllBranchesAction getShowAllBranchesAction(Observable<IRepository> pRepository) {
        return actionFactory.createShowAllBranchesAction(pRepository);
    }

    @Override
    public ShowAllCommitsAction getShowAllCommitsAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranches) {
        return actionFactory.createShowAllCommitsAction(pRepository, pBranches);
    }

    @Override
    public Action getRevertWorkDirAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createRevertWorkDirAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public Action getResetFilesAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        return actionFactory.createResetFilesAction(pRepository, pSelectedFilesObservable);
    }

    @Override
    public Action getResetAction(Observable<IRepository> pRepository, Observable<List<ICommit>> pCommitedFilesObservable) {
        return actionFactory.createResetAction(pRepository, pCommitedFilesObservable);
    }

}
