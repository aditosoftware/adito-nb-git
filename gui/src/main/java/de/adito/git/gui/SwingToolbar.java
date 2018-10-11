package de.adito.git.gui;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.PullAction;
import de.adito.git.gui.actions.PushAction;

import javax.swing.*;
import java.awt.*;

/**
 * @author A.Arnold 11.10.2018
 */

public class SwingToolbar extends JPanel implements ISwingToolbar {
    private IRepository repository;
    private ITopComponent topComponent;
    private BranchListWindow branchListWindow;
    private NewBranchWindow newBranchWindow;

    /**
     * The toolbar for Swing
     *
     * @param pTopComponent       the Frame where the Components have to show
     * @param pBranchListWindow   the BranchListWindow Panel
     * @param pRepositoryProvider the repository
     */
    @Inject
    SwingToolbar(ITopComponent pTopComponent, BranchListWindow pBranchListWindow, RepositoryProvider pRepositoryProvider, NewBranchWindow pNewBranchWindow) {
        super(new BorderLayout());
        topComponent = pTopComponent;
        branchListWindow = pBranchListWindow;
        newBranchWindow = pNewBranchWindow;

        repository = pRepositoryProvider.getRepositoryImpl();
        JToolBar toolBar = new JToolBar("JGit Toolbar");
        _addButtons(toolBar);

        this.add(toolBar, BorderLayout.PAGE_START);
        toolBar.isVisible();
    }

    /**
     * a private helper method to fill the toolbar with the git buttons.
     *
     * @param toolBar the toolbar to fill
     */
    private void _addButtons(JToolBar toolBar) {

        //Branch History Button
        JButton commitHistoryButton = new JButton("Branch History");
        commitHistoryButton.addActionListener(e -> {
            try {
                topComponent.setComponent(branchListWindow);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        toolBar.add(commitHistoryButton);

        //Pull Button
        JButton pullButton = new JButton("Pull");
        pullButton.addActionListener(e -> {
            try {
                new PullAction(repository, "");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        toolBar.add(pullButton);

        //Push Button
        JButton pushButton = new JButton("Push");
        pushButton.addActionListener(e -> {
            try {
                new PushAction(repository);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        toolBar.add(pushButton);

        //New Branch Button
        JButton newBranch = new JButton("new Branch");
        newBranch.addActionListener(e ->
                topComponent.setComponent(newBranchWindow));
        toolBar.add(newBranch);
    }
}
