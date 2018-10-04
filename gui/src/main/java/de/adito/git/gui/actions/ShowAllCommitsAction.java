package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.CommitHistoryWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author A.Arnold 04.10.2018
 */
public class ShowAllCommitsAction extends AbstractAction {

    private JTabbedPane tabbedPane;
    private JTable branchTable;
    private IRepository repository;

    public ShowAllCommitsAction(IRepository pRepository, JTable pCommitTable, JTabbedPane pTabbedPane) {
        putValue(Action.NAME, "Show Commits");
        putValue(Action.SHORT_DESCRIPTION, "Get all commits of this Branch or file");
        branchTable = pCommitTable;
        repository = pRepository;
        tabbedPane = pTabbedPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<ICommit> commits = new ArrayList<>();
        List<IBranch> branches = new ArrayList<>();
        int[] selectedRows = branchTable.getSelectedRows();
        for (int row : selectedRows) {
            try {
                branches.add(repository.getBranch((String) branchTable.getModel().getValueAt(row, 0)));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (IBranch branch : branches) {
                try {
                    commits.addAll(repository.getCommits(branch));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            JPanel jPanel = new JPanel();
            jPanel.add(new CommitHistoryWindow(repository, commits));
            tabbedPane.addTab("Tab", jPanel);
            tabbedPane.updateUI();
        }
    }
}
