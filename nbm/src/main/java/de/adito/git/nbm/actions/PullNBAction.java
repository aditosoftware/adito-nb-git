package de.adito.git.nbm.actions;

import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * An action class to pull the commits of a repository
 *
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.PullNBAction")
@ActionRegistration(displayName = "LBL_PullNBAction_Name")
//Reference for the menu
@ActionReferences({
        @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 100),
        @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 300)
})
public class PullNBAction extends NBAction {

    /**
     * get the actual repository and pull the current branch.
     *
     * @param activatedNodes The activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(activatedNodes);
        Injector injector = IGitConstants.INJECTOR;
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        try {
            actionProvider.getPullAction(repository).actionPerformed(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param activatedNodes The activated nodes of NetBeans
     * @return true if there is one repository for the files
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return findOneRepositoryFromNode(activatedNodes).blockingFirst().isPresent();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PullNBAction.class, "LBL_PullNBAction_Name");
    }

}
