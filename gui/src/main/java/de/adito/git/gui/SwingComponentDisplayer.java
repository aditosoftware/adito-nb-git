package de.adito.git.gui;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;

/**
 * The TopComponent to show the panels
 *
 * @author A.Arnold 11.10.2018
 */
public class SwingComponentDisplayer implements ITopComponentDisplayer {


    private final IWindowProvider windowProvider;

    @Inject
    SwingComponentDisplayer(IWindowProvider pWindowProvider) {
        windowProvider = pWindowProvider;
    }

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
        setComponent(windowProvider.getBranchListWindow(pRepository));
    }

    @Override
    public void showAllCommits(Observable<IRepository> repository, IBranch branch) {
        // TODO: 24.10.2018
    }
}
