package de.adito.git.nbm.vcs;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.repo.RepositoryCache;
import io.reactivex.Observable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSystem;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author a.arnold, 30.10.2018
 */
@VersioningSystem.Registration(displayName = "GitADITO", menuLabel = "GitADITO", metadataFolderNames = ".git", actionsCategory = "GitADITO")
public class GitVersioningSystemImpl extends VersioningSystem {

    private VCSAnnotator annotator;
    private Set<Project> projectCache = new HashSet<>();

    public GitVersioningSystemImpl() {
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        if (annotator == null)
            annotator = new GitAnnotator();
        return annotator;
    }

    @Override
    public File getTopmostManagedAncestor(File file) {
        Project project = FileOwnerQuery.getOwner(file.toURI());
        if (!projectCache.contains(project)) {
            if(_initForProject(project))
                projectCache.add(project);
        }

        if(project == null)
            return null;

        return new File(project.getProjectDirectory().toURI());
    }

    private boolean _initForProject(Project pProject) {
        Observable<IRepository> repo = RepositoryCache.getInstance().findRepository(pProject);
        if (repo != null) {
            repo.flatMap(IRepository::getStatus)
                    .subscribe(pStatus -> {
                        fireStatusChanged(pStatus.getChanged().stream()
                                .map(File::new)
                                .collect(Collectors.toSet()));
                    });
            return true;
        }
        return false;
    }

}
