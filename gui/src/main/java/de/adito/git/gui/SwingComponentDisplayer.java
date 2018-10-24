package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import io.reactivex.Observable;

import javax.swing.*;

/**
 * The TopComponent to show the panels
 *
 * @author A.Arnold 11.10.2018
 */
public class SwingComponentDisplayer implements ITopComponentDisplayer {

    @Override
    public void setComponent(JComponent jComponent) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(jComponent);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void showBranchWindow(Observable<IRepository> pRepository) {
        setComponent(new BranchListWindow(pRepository, this));
    }

    @Override
    public void showAllCommits(Observable<IRepository> repository, IBranch branch) {
        // TODO: 24.10.2018
    }
}
