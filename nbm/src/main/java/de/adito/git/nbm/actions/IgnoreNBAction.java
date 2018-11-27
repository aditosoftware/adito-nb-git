package de.adito.git.nbm.actions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.guice.AditoGitModule;
import de.adito.git.nbm.Guice.AditoNbmModule;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An action class for NetBeans which ignores files for the version control system
 *
 * @author a.arnold, 31.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.IgnoreAction")
@ActionRegistration(displayName = "LBL_IgnoreAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 500)
public class IgnoreNBAction extends NBAction {

    private final Subject<Optional<List<IFileChangeType>>> filesToIgnore = BehaviorSubject.create();

    /**
     * Ignore files in the version control system
     *
     * @param activatedNodes the activated nodes in NetBeans
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(activatedNodes);
        Injector injector = Guice.createInjector(new AditoGitModule(), new AditoNbmModule());
        IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

        List<IFileChangeType> untrackedFiles = repository
                .blockingFirst()
                .orElseThrow(() -> new RuntimeException("no valid repository found"))
                .getStatus()
                .blockingFirst()
                .getUntracked();
        List<FileObject> selectedFiles = new ArrayList<>();
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(FileObject.class) != null) {
                selectedFiles.add(node.getLookup().lookup(FileObject.class));
            }
        }
        final Optional<List<IFileChangeType>> untrackedSelectedFiles = Optional.of(untrackedFiles
                .stream()
                .filter(untrackedFile -> selectedFiles
                        .stream()
                        .anyMatch(selectedFile -> selectedFile.toURI()
                                .equals(untrackedFile.getFile().toURI())))
                .collect(Collectors.toList()));
        filesToIgnore.onNext(untrackedSelectedFiles);
        actionProvider.getIgnoreAction(repository, filesToIgnore);
    }

    /**
     * @param activatedNodes the activated nodes in NetBeans
     * @return true if the can be ignored (no synthetic files) and the files are uncommitted, else false
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(activatedNodes);
        if (repository
                .blockingFirst()
                .orElseThrow(() -> new RuntimeException("no valid repository found"))
                .getStatus()
                .blockingFirst()
                .getUntracked()
                .isEmpty())
            return false;
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(FileObject.class) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(IgnoreNBAction.class, "LBL_IgnoreAction_Name");
    }

}
