package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A utility class for the repository
 *
 * @author a.arnold, 25.10.2018
 */
abstract class NBAction extends NodeAction {

    @NotNull
    static Optional<List<IFileChangeType>> getUncommittedFilesOfNodes(@NotNull Node[] activatedNodes, @NotNull Observable<Optional<IRepository>> pRepository) {
        List<File> files = allFilesofNodes(activatedNodes);
        List<IFileChangeType> uncommittedFiles;

        IRepository repository = pRepository.blockingFirst().orElse(null);
        if (repository == null)
            return Optional.empty();
        uncommittedFiles = repository
                .getStatus()
                .blockingFirst()
                .getUncommitted();
        return Optional.of(uncommittedFiles
                .stream()
                .filter(pUncommittedFile -> files
                        .stream()
                        .anyMatch(pFile -> pUncommittedFile.getFile().getAbsolutePath().equals(pFile.getAbsolutePath())))
                .collect(Collectors.toList()));
    }

    /**
     * @param activatedNodes the active nodes from NetBeans
     * @return a list of Files from activeNodes.
     */
    @NotNull
    private static List<File> allFilesofNodes(@NotNull Node[] activatedNodes) {
        List<File> fileList = new ArrayList<>();
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(FileObject.class) != null) {
                fileList.add(new File(node.getLookup().lookup(FileObject.class).getPath()));
            }
        }
        return fileList;
    }

    /**
     * if the {@code pNode} is another repository than the last git command, return the repository of the {@code pNode}
     *
     * @param activatedNodes The nodes to check the repository
     * @return The repository of the node
     */
    @NotNull
    static Observable<Optional<IRepository>> findOneRepositoryFromNode(@NotNull Node[] activatedNodes) {
        Observable<Optional<IRepository>> repository;
        Project project = null;
        for (Node node : activatedNodes) {
            Project currProject = ProjectUtility.findProject(node);
            if (project != null && currProject != null && !(currProject.equals(project)))
                return BehaviorSubject.createDefault(Optional.empty());
            else
                project = currProject;
        }

        if (project == null) {
            return BehaviorSubject.createDefault(Optional.empty());
        }

        repository = RepositoryCache.getInstance().findRepository(project);
        return repository;
    }

    @Override
    protected abstract void performAction(Node[] nodes);

    @Override
    protected abstract boolean enable(Node[] nodes);

    @Override
    public abstract String getName();

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
