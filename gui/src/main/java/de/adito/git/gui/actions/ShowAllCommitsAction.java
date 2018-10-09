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
 * @author A.Arnold 04.10.2018
 */
public class ShowAllCommitsAction extends AbstractAction {

    private JTable branchTable;
    private IRepository repository;

    public ShowAllCommitsAction(IRepository pRepository, JTable pCommitTable) {
        putValue(Action.NAME, "Show Commits");
        putValue(Action.SHORT_DESCRIPTION, "Get all commits of this Branch or file");
        branchTable = pCommitTable;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        List<ICommit> commits = new ArrayList<>();
        List<IBranch> branches = new ArrayList<>();
        String branchName = null;

        int[] selectedRows = branchTable.getSelectedRows();
        for (int row : selectedRows) {
            try {
                branches.add(repository.getBranch((String) branchTable.getModel().getValueAt(row, 0)));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (IBranch branch : branches) {
                branchName = branch.getName();
                try {
                    commits.addAll(repository.getCommits(branch));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
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
