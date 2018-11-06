package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Action class to show all commits of one branch
 *
 * @author A.Arnold 04.10.2018
 */
class ShowAllCommitsAction extends AbstractTableAction {

    private final IWindowProvider windowProvider;
    private final Observable<List<IBranch>> branches;
    private final Observable<IRepository> repository;

    @Inject
    ShowAllCommitsAction(IWindowProvider pWindowProvider, @Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IBranch>> pBranches) {
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
        branches.blockingFirst().forEach(pBranch -> windowProvider.showCommitHistoryWindow(repository, pBranch));
    }

    private static Observable<Boolean> getIsEnabledObservable(Observable<List<IBranch>> pBranches) {
        return pBranches.map(branchList -> branchList.size() > 0);
    }
}
