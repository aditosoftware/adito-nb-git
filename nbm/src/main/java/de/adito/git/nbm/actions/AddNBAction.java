package de.adito.git.nbm.actions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An action class for NetBeans which adds data to the index
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.AddNBAction")
@ActionRegistration(displayName = "LBL_AddNBAction_Name")
@ActionReferences({
        //Reference for the menu
        @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 100)
})
public class AddNBAction extends NBAction {

    /**
     * @param activatedNodes the activated nodes in Netbeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(activatedNodes);
        Subject<Optional<List<IFileChangeType>>> listFiles = BehaviorSubject.createDefault(getUncommittedFilesOfNodes(activatedNodes, repository));
        Injector injector = Guice.createInjector(new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        actionProvider.getAddAction(repository, listFiles);
    }

    /**
     * @param activatedNodes the activated nodes in Netbeans
     * @return returns true if the files to add are real files (needs only one real file). If the files are synthetic, the return value is false.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            final Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(activatedNodes);
            if (getUncommittedFilesOfNodes(activatedNodes, repository).orElse(Collections.emptyList()).isEmpty()) {
                return false;
            }
            if (node.getLookup().lookup(FileObject.class) != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AddNBAction.class, "LBL_AddNBAction_Name");
    }

}
