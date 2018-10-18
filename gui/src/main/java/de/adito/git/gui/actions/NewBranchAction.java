package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.gui.ITopComponent;
import de.adito.git.gui.NewBranchPanel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NewBranchAction extends AbstractAction {
    private Observable<IRepository> repository;
    private IDialogDisplayer dialogDisplayer;
    private ITopComponent topComponent;

    public  NewBranchAction(Observable<IRepository> pRepository, IDialogDisplayer pDialogDisplayer, ITopComponent pTopComponent){
        this.topComponent = pTopComponent;

        putValue(Action.NAME, "New Branch");
        putValue(Action.SHORT_DESCRIPTION, "Create a new branch in the repository");
        repository = pRepository;
        dialogDisplayer = pDialogDisplayer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            dialogDisplayer.showDialog(new NewBranchPanel(repository, dialogDisplayer), "New Branch", false);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
