package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.ITopComponentDisplayer;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action class for show all branches
 *
 * @author A.Arnold 16.10.2018
 */
public class ShowAllBranchesAction extends AbstractAction {
    private Observable<IRepository> repository;
    private ITopComponentDisplayer topComponentDisplayer;

    /**
     * This is an action to show all branches of one repository.
     *
     * @param pRepository   The observable repository to show all branches
     * @param pTopComponent the top component for the new window
     */
    public ShowAllBranchesAction(Observable<IRepository> pRepository, ITopComponentDisplayer pTopComponent) {
        putValue(Action.NAME, "Show Branches");
        putValue(Action.SHORT_DESCRIPTION, "Get all Branches of this Repository");
        repository = pRepository;
        topComponentDisplayer = pTopComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        topComponentDisplayer.showBranchWindow(repository);
    }

}
