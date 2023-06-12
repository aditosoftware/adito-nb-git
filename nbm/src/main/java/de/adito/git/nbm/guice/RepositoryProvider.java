package de.adito.git.nbm.guice;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.guice.IRepositoryFactory;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.openide.filesystems.FileObject;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  private final Logger logger = Logger.getLogger(RepositoryProvider.class.getName());

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
    logger.log(Level.FINE, () -> String.format("git: retrieving git object for repository in folder %s", repositoryFolder));
    return git.distinctUntilChanged();
  }

  @NonNull
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
