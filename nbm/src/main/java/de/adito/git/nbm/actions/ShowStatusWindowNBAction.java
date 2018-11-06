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
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * @author m.kaspera 06.11.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.ShowStatusWindowNBAction")
@ActionRegistration(displayName = "LBL_ShowStatusWindowNBAction_Name")
@ActionReferences({
        @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 600),
        //Reference for the menu
        @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 600)
})
public class ShowStatusWindowNBAction extends NodeAction {
    @Override
    protected void performAction(Node[] activeNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activeNodes);
        Injector injector = Guice.createInjector(new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);
        actionProvider.getShowStatusWindowAction(repository).actionPerformed(null);
    }

    @Override
    protected boolean enable(Node[] activeNodes) {
        return RepositoryUtility.findOneRepositoryFromNode(activeNodes) != null;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ShowStatusWindowNBAction.class, "LBL_ShowStatusWindowNBAction_Name");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
