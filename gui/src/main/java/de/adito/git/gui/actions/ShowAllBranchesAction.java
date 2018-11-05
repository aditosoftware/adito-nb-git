package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action class to show all branches
 *
 * @author A.Arnold 16.10.2018
 */
@Singleton
class ShowAllBranchesAction extends AbstractAction {
    private Observable<IRepository> repository;
    private IWindowProvider windowProvider;

    /**
     * This is an action to show all branches of one repository.
     *
     * @param pRepository The observable repository to show all branches
     */
    @Inject
    ShowAllBranchesAction(IWindowProvider pWindowProvider, @Assisted Observable<IRepository> pRepository) {
        windowProvider = pWindowProvider;
        repository = pRepository;
        putValue(Action.NAME, "Show Branches");
        putValue(Action.SHORT_DESCRIPTION, "Get all Branches of this Repository");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        windowProvider.showBranchListWindow(repository);
    }

}
