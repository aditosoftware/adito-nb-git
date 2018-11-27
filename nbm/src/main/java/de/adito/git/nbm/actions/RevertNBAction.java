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
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.RevertNBAction")
@ActionRegistration(displayName = "LBL_RevertNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 200)
public class RevertNBAction extends NBAction {

    private final Subject<Optional<List<IFileChangeType>>> selectedFiles = BehaviorSubject.create();

    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(activatedNodes);
        Injector injector = Guice.createInjector(new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        selectedFiles.onNext(getUncommittedFilesOfNodes(activatedNodes, repository));

        actionProvider.getRevertWorkDirAction(repository, selectedFiles);

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return getUncommittedFilesOfNodes(activatedNodes, findOneRepositoryFromNode(activatedNodes)).orElse(Collections.emptyList()).size() > 0;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RevertNBAction.class, "LBL_RevertNBAction_Name");
    }

}
