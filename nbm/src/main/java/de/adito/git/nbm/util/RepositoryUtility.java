package de.adito.git.nbm.util;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.observables.ActiveProjectObservable;
import de.adito.git.nbm.repo.RepositoryCache;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.*;
import org.openide.loaders.DataObject;

import java.util.Optional;

/**
 * @author a.arnold, 20.11.2018
 */
public class RepositoryUtility
{

  private static Observable<Optional<IRepository>> repositoryObservable = null;

  private RepositoryUtility()
  {
  }

  /**
   * get an observable with the repository of the project that contains the currently active file/node/topComponent
   *
   * @return observable with the repository of the project that contains the currently active file/node/topComponent
   */
  public static Observable<Optional<IRepository>> getRepositoryObservable()
  {
    if (repositoryObservable == null)
    {
      repositoryObservable = ActiveProjectObservable.create()
          .switchMap(pOptProj -> pOptProj.map(pProject -> RepositoryCache.getInstance().findRepository(pProject)).orElseGet(() -> Observable.just(Optional.empty())));
    }
    return repositoryObservable;
  }

  /**
   * Find the repository of a dataObject
   *
   * @param pDataObject A {@link DataObject} from NetBeans.
   * @return A repository in which the DataObject is.
   */
  @NotNull
  public static Observable<Optional<IRepository>> find(DataObject pDataObject)
  {
    Project project = FileOwnerQuery.getOwner(pDataObject.getPrimaryFile());
    if (project == null)
      pDataObject.getLookup().lookup(Project.class);
    if (project != null)
      return RepositoryCache.getInstance().findRepository(project);
    return Observable.just(Optional.empty());
  }

}
