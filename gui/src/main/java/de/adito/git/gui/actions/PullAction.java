package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * pullAction to pull from one branch.
 *
 * @author A.Arnold 11.10.2018
 */
class PullAction extends AbstractAction {
    private Observable<Optional<IRepository>> repository;
    private IDialogProvider dialogProvider;

    /**
     * The PullAction is an action to pull all commits from one branch. If no branch is chosen take an empty string for the master branch.
     *
     * @param pRepository the repository where the pull command should work
     */
    @Inject
    PullAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository) {
        dialogProvider = pDialogProvider;
        putValue(Action.NAME, "Pull");
        putValue(Action.SHORT_DESCRIPTION, "Pull all Files from one Branch");
        repository = pRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            List<IMergeDiff> mergeDiffs = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found")).pull();
            if (mergeDiffs.size() > 0) {
                dialogProvider.showMergeConflictDialog(repository, mergeDiffs);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
