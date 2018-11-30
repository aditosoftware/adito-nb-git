package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * pushAction to push all commits to the actual chosen branch.
 * To change the actual branch take the checkout method.
 *
 * @author A.Arnold 11.10.2018
 */
class PushAction extends AbstractAction {
    private Observable<Optional<IRepository>> repository;

    /**
     * @param pRepository The repository to push
     */
    @Inject
    PushAction(@Assisted Observable<Optional<IRepository>> pRepository) {
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
            repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).push();
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
}
