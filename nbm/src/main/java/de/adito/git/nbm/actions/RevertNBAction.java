package de.adito.git.nbm.actions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.guice.AditoGitModule;
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

import java.util.List;

/**
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.RevertNBAction")
@ActionRegistration(displayName = "LBL_RevertNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 200)

public class RevertNBAction extends NodeAction {
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        Injector injector = Guice.createInjector(new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);
        List<IFileChangeType> uncommitedFilesOfNodes = RepositoryUtility.getUncommitedFilesOfNodes(activatedNodes);

//        actionProvider.getRevertWorkDirAction(repository, uncommitedFilesOfNodes);

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return !RepositoryUtility.getUncommitedFilesOfNodes(activatedNodes).isEmpty();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return "Revert";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
