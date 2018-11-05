package de.adito.git.nbm.actions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.util.List;

/**
 * An action class which open the commit dialog
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.CommitNBAction")
@ActionRegistration(displayName = "LBL_CommitNBAction_Name")
@ActionReferences({
        @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 200),
        @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 700)
})

public class CommitNBAction extends NodeAction {

    public CommitNBAction() {
    }

    /**
     * open the commit dialog if the repository is notNull.
     *
     * @param activatedNodes the activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        Injector injector = Guice.createInjector(new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);
        Subject<List<IFileChangeType>> listNodes;

        if (repository != null) {
            if (activatedNodes.length == 0) {
                listNodes = BehaviorSubject.createDefault(repository.blockingFirst().getStatus().blockingFirst().getUncommitted());
            } else {
                listNodes = BehaviorSubject.createDefault(RepositoryUtility.getUncommitedFilesOfNodes(activatedNodes));
            }
            actionProvider.getCommitAction(repository, listNodes).actionPerformed(null);
        }
    }

    /**
     * @param activatedNodes the activated nodes in NetBeans
     * @return return true if the nodes have one repository and there are files which are not committed.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null) {
            System.out.println("DEBUG: CommitNBAction");
            if (RepositoryUtility.findOneRepositoryFromNode(activatedNodes) != null) {
                return !RepositoryUtility.getUncommitedFilesOfNodes(activatedNodes).isEmpty();
            }
            return false;
        }
        return false;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return "Commit";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
