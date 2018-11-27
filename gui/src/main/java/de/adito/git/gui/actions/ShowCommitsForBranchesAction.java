package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action class to show all commits of one branch
 *
 * @author A.Arnold 04.10.2018
 */
class ShowCommitsForBranchesAction extends AbstractTableAction {

    private final IWindowProvider windowProvider;
    private final Observable<Optional<List<IBranch>>> branches;
    private final Observable<Optional<IRepository>> repository;

    @Inject
    ShowCommitsForBranchesAction(IWindowProvider pWindowProvider, @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IBranch>>> pBranches) {
        super("Show Commits", getIsEnabledObservable(pBranches));
        windowProvider = pWindowProvider;
        putValue(Action.NAME, "Show Commits");
        putValue(Action.SHORT_DESCRIPTION, "Get all commits of this Branch or file");
        repository = pRepository;
        branches = pBranches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        branches.blockingFirst()
                .orElse(Collections.emptyList())
                .forEach(pBranch -> windowProvider.showCommitHistoryWindow(repository, pBranch));
    }

    private static Observable<Optional<Boolean>> getIsEnabledObservable(Observable<Optional<List<IBranch>>> pBranches) {
        return pBranches.map(branchList -> Optional.of(branchList.orElse(Collections.emptyList()).size() > 0));
    }
}
