package de.adito.git.nbm;

import com.google.inject.Inject;
import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.impl.IFileSystemObserverProvider;
import de.adito.git.nbm.Guice.IFileSystemObserverImplFactory;

/**
 * @author m.kaspera 15.10.2018
 */
public class FileSystemObserverProviderImpl implements IFileSystemObserverProvider {

    private IFileSystemObserverImplFactory pFactory;

    @Inject
    FileSystemObserverProviderImpl(IFileSystemObserverImplFactory pFactory){
        this.pFactory = pFactory;
    }

    @Override
    public IFileSystemObserver getFileSystemObserver(IRepositoryDescription pRepositoryDescription) {
        return pFactory.create(pRepositoryDescription);
    }
}