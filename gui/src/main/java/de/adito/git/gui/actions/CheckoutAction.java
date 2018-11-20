package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * An action class to checkout to another branch.
 *
 * @author A.Arnold 18.10.2018
 */
class CheckoutAction extends AbstractTableAction {
    private Observable<IRepository> repository;
    private Observable<Optional<IBranch>> branch;

    /**
     * @param pRepository The repository where the branch is
     * @param pBranch     the branch list of selected branches
     */
    @Inject
    CheckoutAction(@Assisted Observable<IRepository> pRepository, @Assisted Observable<Optional<IBranch>> pBranch) {
        super("Checkout", getIsEnabledObservable());
        branch = pBranch;
        putValue(Action.NAME, "Checkout");
        putValue(Action.SHORT_DESCRIPTION, "Command to change the branch to another one");
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (branch.blockingFirst().isPresent()) {
            IBranch branchImpl = branch.blockingFirst().get();
            try {
                repository.blockingFirst().checkout(branchImpl);
                System.out.println("Checkout to: " + branch);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * check the selection of columns in branch list
     *
     * @return return true if the selected list has one element, else false
     */
    private static Observable<Boolean> getIsEnabledObservable() {
        return BehaviorSubject.createDefault(true);
    }
}
