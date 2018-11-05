package de.adito.git.nbm.util;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.nbm.repo.RepositoryCache;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for the repository
 *
 * @author a.arnold, 25.10.2018
 */
public class RepositoryUtility {

    @NotNull
    public static List<IFileChangeType> getUncommitedFilesOfNodes(Node[] activatedNodes) {
        Observable<IRepository> repository = RepositoryUtility.findOneRepositoryFromNode(activatedNodes);
        List<IFileChangeType> fileList = new ArrayList<>();
        List<File> files = allFilesofNodes(activatedNodes);
        List<IFileChangeType> uncommittedFiles = new ArrayList<>();

        if (repository != null) {
            uncommittedFiles = repository.blockingFirst().getStatus().blockingFirst().getUncommitted();
            if (files == null) {
                return fileList;
            }
        }
        for (File file : files) {
            uncommittedFiles.stream()
                    .filter(uncommitFile -> uncommitFile.getFile().getAbsolutePath()
                            .equals(file.getAbsolutePath()))
                    .collect(Collectors.toCollection(() -> fileList));
        }
        return fileList;
    }

    /**
     * @param activatedNodes the active nodes from NetBeans
     * @return a list of Files from activeNodes.
     */
    @NonNull
    private static List<File> allFilesofNodes(Node[] activatedNodes) {
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
    public static Observable<IRepository> findOneRepositoryFromNode(Node[] activatedNodes) {
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
}
