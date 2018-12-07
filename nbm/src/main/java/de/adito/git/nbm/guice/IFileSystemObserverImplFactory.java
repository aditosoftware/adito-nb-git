package de.adito.git.nbm.guice;

import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.nbm.FileSystemObserverImpl;

/**
 * @author m.kaspera 15.10.2018
 */
public interface IFileSystemObserverImplFactory
{

  FileSystemObserverImpl create(IRepositoryDescription pRepositoryDescription);

}
