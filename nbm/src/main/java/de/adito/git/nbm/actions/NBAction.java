package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for the repository
 *
 * @author a.arnold, 25.10.2018
 */
abstract class NBAction extends NodeAction {

    @NotNull
    static List<IFileChangeType> getUncommittedFilesOfNodes(@NotNull Node[] activatedNodes, @Nullable Observable<IRepository> pRepository) {
        List<IFileChangeType> fileList = new ArrayList<>();
        List<File> files = allFilesofNodes(activatedNodes);
        List<IFileChangeType> uncommittedFiles = new ArrayList<>();

        if (pRepository != null) {
            uncommittedFiles = pRepository.blockingFirst().getStatus().blockingFirst().getUncommitted();
        }
        for (File file : files) {
            uncommittedFiles.stream()
                    .filter(uncommittedFile -> uncommittedFile.getFile().getAbsolutePath()
                            .equals(file.getAbsolutePath()))
                    .collect(Collectors.toCollection(() -> fileList));
        }
        return fileList;
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
    @Nullable
    static Observable<IRepository> findOneRepositoryFromNode(@NotNull Node[] activatedNodes) {
        Observable<IRepository> repository;
        Project project = null;
        for (Node node : activatedNodes) {
            Project currProject = ProjectUtility.findProject(node);
            if (project != null && currProject != null && !(currProject.equals(project)))
                return null;
            else
                project = currProject;
        }

        if (project == null) {
            return null;
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
