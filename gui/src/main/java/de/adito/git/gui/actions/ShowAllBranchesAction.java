package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.BranchListWindow;
import de.adito.git.gui.ITopComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowAllBranchesAction extends AbstractAction {
    private IRepository repository;
    private ITopComponent topComponent;


    public ShowAllBranchesAction(IRepository pRepository, ITopComponent pTopComponent) {

        putValue(Action.NAME, "Show Branches");
        putValue(Action.SHORT_DESCRIPTION, "Get all Branches of this Repository");
        repository = pRepository;
        topComponent = pTopComponent;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            topComponent.setComponent(new BranchListWindow(repository));

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
