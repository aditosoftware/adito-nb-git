package de.adito.git.nbm.util;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.repo.RepositoryCache;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;

import java.util.HashSet;

/**
 * @author a.arnold, 20.11.2018
 */
public class RepositoryUtility {

    /**
     * if the {@code pNode} is another repository than the last git command, return the repository of the {@code pNode}
     *
     * @param activatedNodes The nodes to check the repository
     * @return The repository of the node
     */
    @Nullable
    public static Observable<IRepository> findOneRepositoryFromNode(Node[] activatedNodes) {
        HashSet<Observable<IRepository>> repositorySet = new HashSet<>();

        for (Node node : activatedNodes) {
            Project currProject = ProjectUtility.findProject(node);
            if (currProject != null) {
                repositorySet.add(RepositoryCache.getInstance().findRepository(currProject));
            }
        }
        if (repositorySet.size() != 1) {
            return null;
        }
        return repositorySet.iterator().next();
    }

}
