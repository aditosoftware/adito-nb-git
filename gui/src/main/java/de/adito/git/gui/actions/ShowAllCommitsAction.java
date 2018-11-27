package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Action that calls the windowsProvider to display all the commit history with all commits of all branches
 *
 * @author m.kaspera 23.11.2018
 */
public class ShowAllCommitsAction extends AbstractAction {

    private final IWindowProvider windowProvider;
    private final Observable<Optional<IRepository>> repository;

    @Inject
    ShowAllCommitsAction(IWindowProvider pWindowProvider, @Assisted Observable<Optional<IRepository>> pRepository) {
        putValue(Action.NAME, "Show All Commits");
        putValue(Action.SHORT_DESCRIPTION, "Get all commits in this repository");
        windowProvider = pWindowProvider;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        windowProvider.showCommitHistoryWindow(repository, null);
    }
}
