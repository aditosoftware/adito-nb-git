package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.CommitHistoryWindow;
import de.adito.git.gui.ITopComponentDisplayer;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Show all commits of one branch in a new frame
 *
 * @author A.Arnold 04.10.2018
 */
public class ShowAllCommitsAction extends AbstractAction {
    private Observable<List<IBranch>> branches;
    private Observable<IRepository> repository;
    private ITopComponentDisplayer topComponentDisplayer;


    public ShowAllCommitsAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranches, ITopComponentDisplayer pTopComponentDisplayer) {
        topComponentDisplayer = pTopComponentDisplayer;
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
        List<IBranch> iBranches = branches.blockingFirst();
        for (IBranch branch : iBranches) {
            try {
                topComponentDisplayer.showAllCommits(repository, branch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
