package de.adito.git.nbm.guice;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.guice.IRepositoryFactory;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.jetbrains.annotations.*;
import org.openide.filesystems.FileObject;

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
  private final FileObject repositoryFolder;

  @Inject
  RepositoryProvider(IRepositoryFactory pGitFactory, @Assisted FileObject pRepositoryFolder)
  {
    gitFactory = pGitFactory;
    repositoryFolder = pRepositoryFolder;
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

  @NotNull
  public FileObject getRepositoryFolder()
  {
    return repositoryFolder;
  }

  public void setRepositoryDescription(@Nullable IRepositoryDescription pDescription)
  {
    if (pDescription != null)
      git.onNext(Optional.of(gitFactory.create(pDescription)));
    else
      git.onNext(Optional.empty());
  }

}
