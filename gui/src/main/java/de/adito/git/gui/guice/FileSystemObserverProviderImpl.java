package de.adito.git.gui.guice;

import com.google.inject.Singleton;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.impl.IFileSystemObserverProvider;

/**
 * This class is a observer but in this case never used.
 * This file is only for guice
 *
 * @author a.arnold, 31.10.2018
 */
@Singleton
class FileSystemObserverProviderImpl implements IFileSystemObserverProvider
{

  @Override
  public IFileSystemObserver getFileSystemObserver(IRepositoryDescription pRepositoryDescription)
  {
    throw new RuntimeException("de.adito.git.gui.guice.FileSystemObserverProviderImpl.getFileSystemObserver");
  }

}
