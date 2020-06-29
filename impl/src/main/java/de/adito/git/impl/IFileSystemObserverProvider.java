package de.adito.git.impl;

import de.adito.git.api.IFileSystemObserver;
import de.adito.git.api.data.IRepositoryDescription;

/**
 * @author m.kaspera 15.10.2018
 */
public interface IFileSystemObserverProvider {

    IFileSystemObserver getFileSystemObserver(IRepositoryDescription pRepositoryDescription);
}
