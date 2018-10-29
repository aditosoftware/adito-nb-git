package de.adito.git.nbm.actions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.gui.ITopComponentDisplayer;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.guice.AditoGitModule;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.Observable;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.awt.event.ActionEvent;

/**
 * Action class for NetBeans that allow to show all branches.
 *
 * @author a.arnold, 22.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.AllBranchNBAction")
@ActionRegistration(displayName = "AllBranches")
@ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 100)
public class AllBranchNBAction extends NodeAction {

    private IActionProvider actionProvider;

    AllBranchNBAction() {
        Injector injector = Guice.createInjector(new AditoGitModule(), new AditoNbmModule());
        actionProvider = injector.getInstance(IActionProvider.class);
    }

    /**
     * @return Returns the Name of the action.
     */
    @Override
    public String getName() {
        return "All branches";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * Perform an action that open a ShowAllBranchesAction in a new {@link ITopComponentDisplayer}
     *
     * @param activatedNodes The active nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<IRepository> repository;
        if (activatedNodes.length == 1) {
            Project project = ProjectUtility.findProject(activatedNodes[0]);
            if (project != null) {
                repository = RepositoryCache.getInstance().findRepository(project);

                actionProvider.getShowAllBranchesAction(repository).actionPerformed(new ActionEvent(this, -1, null));
            }
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
        for (Node node : activatedNodes) {
            Project project = ProjectUtility.findProject(node);
            if (project != null) {
                return ProjectUtility.checkAndChangeRepository(project);
            }
        }
        return false;
    }
}
