package de.adito.git.nbm.vcs;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.nbm.repo.RepositoryCache;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.FileOwnerQuery;

import java.io.File;
import java.util.Optional;

/**
 * @author a.arnold, 12.12.2018
 */
class GitVCSUtility
{

  private GitVCSUtility()
  {
  }

  /**
   * @param pFile A File
   * @return Returns a {@link IFileChangeType} with the actual changeType
   */
  @Nullable
  static IFileChangeType findChanges(@NotNull File pFile)
  {
    Optional<IRepository> repositoryOpt = RepositoryCache.getInstance()
        .findRepository(FileOwnerQuery.getOwner(pFile.toURI())).blockingFirst(Optional.empty());
    if (!repositoryOpt.isPresent())
      return null;
    IRepository repository = repositoryOpt.get();
    return repository.getStatusOfSingleFile(pFile);
  }

}
