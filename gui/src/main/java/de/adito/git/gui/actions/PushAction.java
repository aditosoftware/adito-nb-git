package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * pushAction to push all commits to the actual chosen branch.
 * To change the actual branch take the checkout method.
 *
 * @author A.Arnold 11.10.2018
 */
public class PushAction extends AbstractAction {
    private Observable<IRepository> repository;

    /**
     * @param pRepository The repository to push
     */
    public PushAction(Observable<IRepository> pRepository) {
        putValue(Action.NAME, "Push");
        putValue(Action.SHORT_DESCRIPTION, "Pull all added files to one Branch or Master Branch");
        repository = pRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        repository.subscribe(IRepository::push);
    }
}
