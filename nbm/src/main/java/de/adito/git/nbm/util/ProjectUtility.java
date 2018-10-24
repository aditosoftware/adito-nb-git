package de.adito.git.nbm.util;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.repo.RepositoryCache;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * A util class for a good workflow
 *
 * @author a.arnold, 22.10.2018
 */
public class ProjectUtility {

    /**
     * if the {@code pNode} is another repository than the last git command, return the repository of the {@code pNode}
     *
     * @param pProject the repository to check
     * @return the repository of the node
     */
    public static boolean checkAndChangeRepository(@NotNull Project pProject) {
        Observable<IRepository> repo = RepositoryCache.getInstance().findRepository(pProject);
        return true;
    }

    /**
     * Find a project for a {@link Node} component
     *
     * @param pNode {@link Node} component for wich a project should be found
     * @return The project of a {@link Node}
     */
    @Nullable
    public static Project findProject(@NotNull Node pNode) {
        return findProject(pNode.getLookup());
    }

    /**
     * Find a project for a {@link Lookup} component
     *
     * @param pLookup {@link Lookup} component for which a project should be found
     * @return The project of a {@link Lookup}
     */
    @Nullable
    public static Project findProject(@NotNull Lookup pLookup) {
        Project project = pLookup.lookup(Project.class);
        if (project == null) {
            FileObject fo = pLookup.lookup(FileObject.class);
            if (fo != null)
                project = FileOwnerQuery.getOwner(fo);
            else {
                DataObject dataObject = pLookup.lookup(DataObject.class);
                if (dataObject != null) {
                    fo = dataObject.getPrimaryFile();
                    if (fo != null)
                        project = FileOwnerQuery.getOwner(fo);
                }
            }
        }
        return project;
    }

}
