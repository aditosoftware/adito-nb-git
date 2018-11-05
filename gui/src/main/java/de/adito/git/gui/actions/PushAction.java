package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
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
@Singleton
class PushAction extends AbstractAction {
    private Observable<IRepository> repository;

    /**
     * @param pRepository The repository to push
     */
    @Inject
    PushAction(@Assisted Observable<IRepository> pRepository) {
        putValue(Action.NAME, "Push");
        putValue(Action.SHORT_DESCRIPTION, "Pull all added files to one Branch or Master Branch");
        repository = pRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.blockingFirst().push();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
