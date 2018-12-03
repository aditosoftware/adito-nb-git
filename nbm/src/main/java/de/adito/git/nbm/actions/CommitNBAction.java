package de.adito.git.nbm.actions;

import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

public class CommitNBAction extends NBAction {

    public CommitNBAction() {
    }

    /**
     * open the commit dialog if the repository is notNull.
     *
     * @param activatedNodes the activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(activatedNodes);
        Injector injector = IGitConstants.INJECTOR;
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);
        Subject<Optional<List<IFileChangeType>>> listNodes;

        if (activatedNodes.length == 0) {
            listNodes = BehaviorSubject.createDefault(Optional.of(repository.blockingFirst()
                    .orElseThrow(() -> new RuntimeException("no valid repository found"))
                    .getStatus()
                    .blockingFirst()
                    .getUncommitted()));
        } else {
            listNodes = BehaviorSubject.createDefault(getUncommittedFilesOfNodes(activatedNodes, repository));
        }
        actionProvider.getCommitAction(repository, listNodes).actionPerformed(null);
    }

    @Override
    protected String iconResource() {
        return NbBundle.getMessage(PushNBAction.class, "ICON_CommitNBAction_Path");
    }

    /**
     * @param activatedNodes the activated nodes in NetBeans
     * @return return true if the nodes have one repository and there are files which are not committed.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null) {
            Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(activatedNodes);
            return !getUncommittedFilesOfNodes(activatedNodes, repository).orElse(Collections.emptyList()).isEmpty();
        }
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CommitNBAction.class, "LBL_CommitNBAction_Name");
    }
}
