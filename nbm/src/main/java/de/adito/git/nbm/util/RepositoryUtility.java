package de.adito.git.nbm.util;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.repo.RepositoryCache;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;

import java.util.*;

/**
 * @author a.arnold, 20.11.2018
 */
public class RepositoryUtility
{
  private RepositoryUtility()
  {
  }

  /**
   * if the {@code pNode} is another repository than the last git command used , return the repository of the {@code pNode}
   *
   * @param pActivatedNodes The nodes to check the repository
   * @return The repository of the node
   */
  @Nullable
  public static Observable<Optional<IRepository>> findOneRepositoryFromNode(Node[] pActivatedNodes)
  {
    HashSet<Observable<Optional<IRepository>>> repositorySet = new HashSet<>();
    if (pActivatedNodes == null)
    {
      return Observable.just(Optional.empty());
    }
    for (Node node : pActivatedNodes)
    {
      Project currProject = ProjectUtility.findProject(node);
      if (currProject != null)
      {
        repositorySet.add(RepositoryCache.getInstance().findRepository(currProject));
      }
    }
    if (repositorySet.size() != 1)
    {
      return Observable.just(Optional.empty());
    }
    return repositorySet.iterator().next();
  }

}
