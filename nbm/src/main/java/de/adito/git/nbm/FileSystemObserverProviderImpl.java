package de.adito.git.nbm;

import com.google.inject.Inject;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.impl.IFileSystemObserverProvider;
import de.adito.git.nbm.guice.IFileSystemObserverImplFactory;

import java.util.WeakHashMap;

/**
 * @author m.kaspera 15.10.2018
 */
public class FileSystemObserverProviderImpl implements IFileSystemObserverProvider
{

  private IFileSystemObserverImplFactory pFactory;
  private WeakHashMap<IRepositoryDescription, IFileSystemObserver> cache = new WeakHashMap<>();

  @Inject
  FileSystemObserverProviderImpl(IFileSystemObserverImplFactory pFactory)
  {
    this.pFactory = pFactory;
  }

  @Override
  public IFileSystemObserver getFileSystemObserver(IRepositoryDescription pRepositoryDescription)
  {
    return cache.computeIfAbsent(pRepositoryDescription, pDescription -> pFactory.create(pDescription));
  }
}
