package de.adito.git.nbm.util;

import org.jetbrains.annotations.*;
import org.netbeans.api.project.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * A util class for a good workflow
 *
 * @author a.arnold, 22.10.2018
 */
public class ProjectUtility
{
  private ProjectUtility()
  {
  }

  /**
   * Find a project for a {@link Node} component
   *
   * @param pNode {@link Node} component for wich a project should be found
   * @return The project of a {@link Node}
   */
  @Nullable
  public static Project findProject(@NotNull Node pNode)
  {
    return _findProject(pNode.getLookup());
  }

  /**
   * Find a project for a {@link Lookup} component
   *
   * @param pLookup {@link Lookup} component for which a project should be found
   * @return The project of a {@link Lookup}
   */
  @Nullable
  private static Project _findProject(@NotNull Lookup pLookup)
  {
    Project project = pLookup.lookup(Project.class);
    if (project == null)
    {
      FileObject fo = pLookup.lookup(FileObject.class);
      if (fo != null)
        project = FileOwnerQuery.getOwner(fo);
      else
      {
        DataObject dataObject = pLookup.lookup(DataObject.class);
        if (dataObject != null)
        {
          fo = dataObject.getPrimaryFile();
          if (fo != null)
            project = FileOwnerQuery.getOwner(fo);
        }
      }
    }
    return project;
  }


}
