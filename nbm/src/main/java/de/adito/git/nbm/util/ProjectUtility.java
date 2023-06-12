package de.adito.git.nbm.util;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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
  public static Project findProject(@NonNull Node pNode)
  {
    return _findProject(pNode.getLookup());
  }

  /**
   * tries to find a project from any of the activated nodes or the activated topComponent.
   * Returns the first found Project or null if none can be found anywhere
   *
   * @param pTopComponentRegistry Netbeans TopComponentRegistry
   * @return currently selected project, wrapped in Optional in case no project can be found
   */
  @NonNull
  public static Optional<Project> findProjectFromActives(TopComponent.Registry pTopComponentRegistry)
  {
    TopComponent activatedTopComponent = pTopComponentRegistry.getActivated();
    if (activatedTopComponent == null)
      return Optional.empty();
    Project project = _findProject(activatedTopComponent.getLookup());
    if (project == null)
    {
      project = _getProjectFromActiveNodes(pTopComponentRegistry);
    }
    if (project == null)
    {
      project = _getProjectFromTopComponentNodes(activatedTopComponent);
    }
    return Optional.ofNullable(project);
  }

  /**
   * Find a project for a {@link Lookup} component
   *
   * @param pLookup {@link Lookup} component for which a project should be found
   * @return The project of a {@link Lookup}
   */
  @Nullable
  private static Project _findProject(@NonNull Lookup pLookup)
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

  /**
   * tries to find a project in the nodes from the active TopComponent
   *
   * @param pActivatedTopComponent Netbeans TopComponentRegistry
   * @return Project if any was found, null otherwise
   */
  @Nullable
  private static Project _getProjectFromTopComponentNodes(TopComponent pActivatedTopComponent)
  {
    Node foundNode = pActivatedTopComponent.getLookup().lookup(Node.class);
    if (foundNode != null)
      return ProjectUtility.findProject(foundNode);
    return null;
  }

  /**
   * tries to find a project in the activated nodes from the topComponentRegistry
   *
   * @param pTopComponentRegistry Netbeans TopComponentRegistry
   * @return Project if any was found, null otherwise
   */
  @Nullable
  private static Project _getProjectFromActiveNodes(TopComponent.Registry pTopComponentRegistry)
  {
    Optional<Project> projectFromActiveNodes = Arrays.stream(pTopComponentRegistry.getActivatedNodes())
        .map(pActiveNode -> _findProject(pActiveNode.getLookup()))
        .filter(Objects::nonNull)
        .findFirst();
    return projectFromActiveNodes.orElse(null);
  }


}
