package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * pullAction to pull from one branch.
 *
 * @author A.Arnold 11.10.2018
 */
class PullAction extends AbstractAction {
    private String targetId;
    private Observable<IRepository> repository;

    /**
     * The PullAction is an action to pull all commits from one branch. If no branch is chosen take an empty string for the master branch.
     *
     * @param pRepository the repository where the pull command should work
     * @param pTargetId   the ID of the branch which is to pull
     */
    @Inject
    PullAction(@Assisted Observable<IRepository> pRepository, @Assisted String pTargetId) {
        putValue(Action.NAME, "Pull");
        putValue(Action.SHORT_DESCRIPTION, "Pull all Files from one Branch");
        targetId = pTargetId;
        repository = pRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.blockingFirst().pull(targetId);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
