package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * pullAction to pull from one branch.
 * @author A.Arnold 11.10.2018
 */

public class PullAction extends AbstractAction {
    private String targetId;
    private IRepository repository;

    /**
     * The PullAction is an action to pull all commits from one branch. If no branch is chosen take an empty string for the master branch.
     * @param pRepository the repository to choose
     * @param pTargetId the ID of one branch
     */
    public PullAction(IRepository pRepository, String pTargetId){
        putValue(Action.NAME, "Pull");
        putValue(Action.SHORT_DESCRIPTION, "Pull all Files from one Branch");
        targetId = pTargetId;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.pull(targetId);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
