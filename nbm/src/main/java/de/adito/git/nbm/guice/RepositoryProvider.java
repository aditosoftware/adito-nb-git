package de.adito.git.nbm.guice;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.guice.IRepositoryFactory;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Gets the Repository object injected and forms the central place to get it from
 *
 * @author m.kaspera 27.09.2018
 */
public class RepositoryProvider implements IRepositoryProvider
{

  private final Subject<Optional<IRepository>> git;
  private final IRepositoryFactory gitFactory;

  @Inject
  RepositoryProvider(IRepositoryFactory pGitFactory)
  {
    gitFactory = pGitFactory;
    git = BehaviorSubject.createDefault(Optional.empty());
  }

  /**
   * @return IRepository implementation of the IRepository interface
   */
  @Override
  public Observable<Optional<IRepository>> getRepositoryImpl()
  {
    return git.distinctUntilChanged();
  }

  public void setRepositoryDescription(@Nullable IRepositoryDescription pDescription)
  {
    if (pDescription != null)
      git.onNext(Optional.of(gitFactory.create(pDescription)));
    else
      git.onNext(Optional.empty());
  }

}
