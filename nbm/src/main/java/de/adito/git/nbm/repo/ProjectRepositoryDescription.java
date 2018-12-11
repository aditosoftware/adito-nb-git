package de.adito.git.nbm.repo;

import de.adito.git.api.data.IRepositoryDescription;
import org.jetbrains.annotations.Nullable;
import org.openide.filesystems.FileObject;

/**
 * @author a.arnold, 22.10.2018
 */
public class ProjectRepositoryDescription implements IRepositoryDescription
{
  private final FileObject projectDirectory;

  public ProjectRepositoryDescription(FileObject pProjectDirectory)
  {
    projectDirectory = pProjectDirectory;
  }

  @Override
  public String getPath()
  {
    return projectDirectory.getPath();
  }

  @Override
  public String getEmail()
  {
    return null;
  }

  @Override
  public String getUsername()
  {
    return null;
  }

  @Override
  public @Nullable String getPassword()
  {
    return null;
  }

  @Override
  public @Nullable String getSSHKeyLocation()
  {
    return null;
  }

  @Override
  public @Nullable String getPassphrase()
  {
    return null;
  }
}
