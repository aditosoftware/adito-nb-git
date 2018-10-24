package de.adito.git.nbm.repo;

import de.adito.git.api.data.IRepositoryDescription;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;

import java.io.File;

/**
 * @author a.arnold, 22.10.2018
 */
public class ProjectRepositoryDescription implements IRepositoryDescription
{

    private Project project;

    public ProjectRepositoryDescription(Project pProject) {
        project = pProject;
    }

    @Override
    public String getPath() {
        return project.getProjectDirectory().getPath();
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public @Nullable String getSSHKeyLocation() {
        return null;
    }

    @Override
    public @Nullable String getPassphrase() {
        return null;
    }
}
