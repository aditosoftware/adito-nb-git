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
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * An action class for NetBeans to show all branches.
 *
 * @author a.arnold, 22.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.AllBranchNBAction")
@ActionRegistration(displayName = "LBL_ShowALlBranchesNBAction_Name")
@ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 500)
public class AllBranchNBAction extends NodeAction {

    /**
     * @param activatedNodes The active nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        if (repository != null) {
            Injector injector = Guice.createInjector(new AditoNbmModule());
            IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

            actionProvider.getShowAllBranchesAction(repository).actionPerformed(null);
        }
    }

    /**
     * Checking the entry point of the class {@link AllBranchNBAction}
     *
     * @param activatedNodes The active nodes in Netbeans
     * @return returns true if the activated project has an repository, else false.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return RepositoryUtility.findOneRepositoryFromNode(activatedNodes) != null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * @return Returns the Name of the action.
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(AllBranchNBAction.class, "LBL_ShowALlBranchesNBAction_Name");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
