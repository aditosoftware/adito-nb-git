package de.adito.git.nbm.actions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.Observable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * An action class to pull the commits of a repository
 *
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.PullNBAction")
@ActionRegistration(displayName = "LBL_PullNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 100)
public class PullNBAction extends NodeAction {

    /**
     * get the actual repository and pull the current branch.
     *
     * @param activatedNodes The activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        Injector injector = Guice.createInjector(new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        if (repository != null) {
            try {
                actionProvider.getPullAction(repository, repository.blockingFirst().getCurrentBranch());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param activatedNodes The activated nodes of NetBeans
     * @return true if there is one repository for the files
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return RepositoryUtility.findOneRepositoryFromNode(activatedNodes) != null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return "Push";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
