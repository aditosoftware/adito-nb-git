package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * pushAction to push all commits to the actual chosen branch.
 * To change the actual branch take the checkout method.
 *
 * @author A.Arnold 11.10.2018
 */
public class PushAction extends AbstractAction {
    private IRepository repository;

    public PushAction(IRepository pRepository) {
        putValue(Action.NAME, "Push");
        putValue(Action.SHORT_DESCRIPTION, "Pull all added files to one Branch or Master Branch");
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            repository.push();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
