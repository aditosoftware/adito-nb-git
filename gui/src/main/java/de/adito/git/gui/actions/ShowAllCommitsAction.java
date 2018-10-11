package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.CommitHistoryWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Show all commits of one branch in new frames
 *
 * @author A.Arnold 04.10.2018
 */
public class ShowAllCommitsAction extends AbstractAction {

    private List<IBranch> branches;
    private IRepository repository;

    public ShowAllCommitsAction(IRepository pRepository, List<IBranch> pBranches) {
        putValue(Action.NAME, "Show Commits");
        putValue(Action.SHORT_DESCRIPTION, "Get all commits of this Branch or file");
        repository = pRepository;
        branches = pBranches;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        List<ICommit> commits = new ArrayList<>();
        String branchName = null;
        for (IBranch branch : branches) {
            branchName = branch.getName();
            try {
                commits.addAll(repository.getCommits(branch));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            JFrame commitFrame = new JFrame();
            JPanel panel = new JPanel();
            commitFrame.add(BorderLayout.CENTER, panel);
            commitFrame.setPreferredSize(new Dimension(800, 300));
            panel.add(branchName, new CommitHistoryWindow(repository, commits));
            commitFrame.pack();
            commitFrame.setVisible(true);
        }
    }
}
