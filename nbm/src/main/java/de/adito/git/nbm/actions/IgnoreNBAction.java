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
import io.reactivex.subjects.BehaviorSubject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

import javax.tools.FileObject;
import java.util.ArrayList;
import java.util.List;

/**
 * An action class for NeBeans which ignore files for the version control system
 *
 * @author a.arnold, 31.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.IgnoreAction")
@ActionRegistration(displayName = "LBL_IgnoreAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 500)
public class IgnoreNBAction extends NodeAction {

    /**
     * Ignore files in the version control system
     *
     * @param activatedNodes the activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        Injector injector = Guice.createInjector(new AditoGitModule(), new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        if (repository != null) {
            List<IFileChangeType> untrackedFiles = repository.blockingFirst().getStatus().blockingFirst().getUntracked();
            Observable<List<IFileChangeType>> fileList = BehaviorSubject.createDefault(untrackedFiles);
            actionProvider.getIgnoreAction(repository, fileList);
        }
    }

    /**
     * @param activatedNodes the activated nodes in NetBeans
     * @return true if the can be ignored (no synthetic files) and the files are uncommitted, else false
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        List<IFileChangeType> untrackedFiles = new ArrayList<>();
        if (repository != null) {
            untrackedFiles = repository.blockingFirst().getStatus().blockingFirst().getUntracked();
        }
        if (untrackedFiles.isEmpty()) {
            return false;
        }
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(FileObject.class) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(IgnoreNBAction.class, "LBL_IgnoreAction_Name");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
