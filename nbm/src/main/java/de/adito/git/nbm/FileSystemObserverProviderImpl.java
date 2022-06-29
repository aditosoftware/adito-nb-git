package de.adito.git.nbm;

import de.adito.git.api.*;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.impl.IFileSystemObserverProvider;

/**
 * @author m.kaspera 15.10.2018
 */
public class FileSystemObserverProviderImpl implements IFileSystemObserverProvider
{

  @Override
  public IFileSystemObserver getFileSystemObserver(IRepositoryDescription pRepositoryDescription, IIgnoreFacade pGitIgnoreFacade)
  {
    return new FileSystemObserverImpl(pRepositoryDescription, pGitIgnoreFacade);
  }

}
