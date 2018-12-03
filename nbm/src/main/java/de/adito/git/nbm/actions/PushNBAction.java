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
 * An action class to push all current commits
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.PushNBAction")
@ActionRegistration(displayName = "LBL_PushNBAction_Name")
@ActionReferences({
        //Reference for the toolbar
        @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 300),
        //Reference for the menu
        @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 200)
})
public class PushNBAction extends NBAction {

    /**
     * @param activatedNodes The activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(activatedNodes);
        Injector injector = IGitConstants.INJECTOR;
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);
        actionProvider.getPushAction(repository).actionPerformed(null);
    }

    @Override
    protected String iconResource() {
        return NbBundle.getMessage(PushNBAction.class, "ICON_PushNBAction_Path");
    }

    /**
     * @param activatedNodes The activated nodes in NetBeans
     * @return return true, if there is one repository for the nodes
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return findOneRepositoryFromNode(activatedNodes).blockingFirst().isPresent();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PushNBAction.class, "LBL_PushNBAction_Name");
    }

}
