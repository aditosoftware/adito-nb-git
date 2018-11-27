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
import java.util.Optional;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public MergeAction getMergeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pTargetBranch) {
        return actionFactory.createMergeAction(pRepository, pTargetBranch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommitAction getCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createCommitAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffAction getDiffAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createDiffAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IgnoreAction getIgnoreAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createIgnoreAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddAction getAddAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createAddAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExcludeAction getExcludeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createExcludeAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutAction getCheckoutAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranch) {
        return actionFactory.createCheckoutAction(pRepository, pBranch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NewBranchAction getNewBranchAction(Observable<Optional<IRepository>> pRepository) {
        return actionFactory.createNewBranchAction(pRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PullAction getPullAction(Observable<Optional<IRepository>> pRepository, String pTargetId) {
        return actionFactory.createPullAction(pRepository, pTargetId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PushAction getPushAction(Observable<Optional<IRepository>> pRepository) {
        return actionFactory.createPushAction(pRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShowAllBranchesAction getShowAllBranchesAction(Observable<Optional<IRepository>> pRepository) {
        return actionFactory.createShowAllBranchesAction(pRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShowCommitsForBranchesAction getShowAllCommitsAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IBranch>>> pBranches) {
        return actionFactory.createShowAllCommitsAction(pRepository, pBranches);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getRevertWorkDirAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createRevertWorkDirAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getResetFilesAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable) {
        return actionFactory.createResetFilesAction(pRepository, pSelectedFilesObservable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getResetAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pCommittedFilesObservable) {
        return actionFactory.createResetAction(pRepository, pCommittedFilesObservable);
    }

    @Override
    public Action getShowStatusWindowAction(Observable<Optional<IRepository>> pRepository) {
        return actionFactory.createShowStatusWindowAction(pRepository);
    }

}
