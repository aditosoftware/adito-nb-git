package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.IDialogDisplayer;
import de.adito.git.gui.NewBranchPanel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This action class creates a newBranchPanel.
 * Also the action open the createBranch in {@link IRepository}. There is an automatically call for the remote
 *
 * @author A.Arnold 18.10.2018
 */
class NewBranchAction extends AbstractAction {
    private Observable<IRepository> repository;
    private IDialogDisplayer dialogDisplayer;
    private NewBranchPanel newBranchPanel;

    /**
     * @param pRepository      The repository where the new branch should exists
     * @param pDialogDisplayer The Interface to provide functionality of giving an overlying framework
     */
    @Inject
    NewBranchAction(IDialogDisplayer pDialogDisplayer, @Assisted Observable<IRepository> pRepository) throws Exception {
        putValue(Action.NAME, "New Branch");
        putValue(Action.SHORT_DESCRIPTION, "Create a new branch in the repository");
        repository = pRepository;
        dialogDisplayer = pDialogDisplayer;
        newBranchPanel = new NewBranchPanel(repository, dialogDisplayer);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (dialogDisplayer.showDialog(newBranchPanel, "New Branch", false)) {
                repository.blockingFirst().createBranch(newBranchPanel.getBranchName(), newBranchPanel.getCheckoutValid());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
