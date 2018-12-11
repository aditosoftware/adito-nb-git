package de.adito.git.nbm.vcs;

import org.netbeans.api.project.*;
import org.netbeans.modules.versioning.spi.*;

import java.io.File;

/**
 * @author a.arnold, 30.10.2018
 */
@SuppressWarnings("unused")
@VersioningSystem.Registration(displayName = "GitADITO", menuLabel = "GitADITO", metadataFolderNames = ".git", actionsCategory = "GitADITO")
public class GitVersioningSystemImpl extends VersioningSystem
{
  private VCSAnnotator annotator;

  @Override
  public VCSAnnotator getVCSAnnotator()
  {
    if (annotator == null)
      annotator = new GitAnnotator();
    return annotator;
  }

  @Override
  public File getTopmostManagedAncestor(File pFile)
  {
    Project project = FileOwnerQuery.getOwner(pFile.toURI());
    if (project == null)
      return null;
    return new File(project.getProjectDirectory().toURI());
  }
}
