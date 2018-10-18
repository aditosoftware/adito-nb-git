package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * An action class to checkout to another branch.
 *
 * @author A.Arnold 18.10.2018
 */
public class CheckoutAction extends AbstractAction {
    private Observable<IRepository> repository;
    private Observable<List<IBranch>> branchList;

    /**
     * @param pRepository The repository where the branch is
     * @param pBranchList the Branchlist of selected branches
     */
    public CheckoutAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranchList) {
        branchList = pBranchList;
        putValue(Action.NAME, "Checkout");
        putValue(Action.SHORT_DESCRIPTION, "Command to change the branch to another one");
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        IBranch branch = branchList.blockingFirst().get(0);
        try {
            repository.blockingFirst().checkout(branch);
            System.out.println("Checkout to: " + branch);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * check the selection of columns in branch list
     *
     * @return return true if the selected list has one element, else false
     */
    @Override
    public boolean isEnabled() {
        return branchList.blockingFirst().size() == 1;
    }
}
