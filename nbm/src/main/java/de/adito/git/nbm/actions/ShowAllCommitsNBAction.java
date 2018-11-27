package de.adito.git.nbm.actions;

import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * NetBeans Action for calling getting a display with the commit history for all commits of all
 * branches for the selected project
 *
 * @author m.kaspera 27.11.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.ShowAllCommitsNBAction")
@ActionRegistration(displayName = "LBL_ShowCommitLogNBAction_Name")
@ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 800)
public class ShowAllCommitsNBAction extends NBAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(activatedNodes);
        Injector injector = IGitConstants.INJECTOR;
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        actionProvider.getShowAllCommitsAction(repository).actionPerformed(null);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return findOneRepositoryFromNode(activatedNodes).blockingFirst().isPresent();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ShowAllCommitsNBAction.class, "LBL_ShowCommitLogNBAction_Name");
    }
}
