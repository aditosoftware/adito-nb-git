package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.window.IWindowProvider;
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
class ShowAllCommitsAction extends AbstractAction {
    private final IWindowProvider windowProvider;
    private Observable<List<IBranch>> branches;
    private Observable<IRepository> repository;

    @Inject
    ShowAllCommitsAction(IWindowProvider pWindowProvider, @Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IBranch>> pBranches) {
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
        List<IBranch> iBranches = branches.blockingFirst();
        for (IBranch branch : iBranches) {
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
                panel.add(branchName, windowProvider.getCommitHistoryWindow(repository, commits));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
