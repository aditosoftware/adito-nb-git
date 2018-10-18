package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.CommitHistoryWindow;
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

    public ShowAllCommitsAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranches) {
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
        List<ICommit> commits = new ArrayList<>();
        List<IBranch> iBranches = branches.blockingFirst();

        for (IBranch branch : iBranches) {
            String branchName = branch.getName();
            try {
                commits.addAll(repository.blockingFirst().getCommits(branch));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            JFrame commitFrame = new JFrame();
            JPanel panel = new JPanel();
            commitFrame.add(BorderLayout.CENTER, panel);
            commitFrame.setPreferredSize(new Dimension(800, 300));
            try {
                panel.add(branchName, new CommitHistoryWindow(repository, commits));
            } catch (Exception e) {
                e.printStackTrace();
            }
            commitFrame.pack();
            commitFrame.setVisible(true);
        }
    }
}
